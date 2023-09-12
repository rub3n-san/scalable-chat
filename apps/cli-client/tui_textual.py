from datetime import datetime
from textual.app import App, ComposeResult, RenderResult
from textual.widgets import Placeholder,  RichLog, Input, Header, Static, Button, Label, Footer,ListView , ListItem
from textual.events import Click
from textual.widget import Widget
from textual.scroll_view import ScrollView
from textual.containers import  VerticalScroll, Container, Grid, Horizontal, Vertical
from textual.screen import Screen, ModalScreen
from dataclasses import dataclass
import argparse

import json
import websockets
import re
import requests

def replace_ip_with_localhost(url: str) -> str:
    # Regular expression to find the IP address format in the URL
    pattern = r"ws://\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}"
    return re.sub(pattern, "ws://localhost", url)

def round_fractional_seconds(s):
    # Split the string at the decimal point
    before_decimal, after_decimal = s.split('.')
    # Take the first 6 characters after the decimal and round
    rounded = round(float(f".{after_decimal[:7]}"))
    return f"{before_decimal}.{str(rounded).zfill(6)}"

def format_date( message):
    data_string = round_fractional_seconds(message['createdAt'])
    dt = datetime.strptime(data_string, '%Y-%m-%dT%H:%M:%S.%f')
    return f"[dim]{dt.strftime('%H:%M:%S')}[/dim]"
    
def format_user_name(message, itsMe):
    if itsMe: 
        color = "blue"
    else:
        color = "green"

    return f"[bold {color}]{message['userName']}[/bold {color}]"

def fetch_websocket_and_latest_messages(url, channel, user):
    def ensure_trailing_slash(url: str) -> str:
        return url if url.endswith('/') else url + '/'
    
    url = f"{ensure_trailing_slash(url)}{channel}"
    params = {'snapshot': 'true'}
    headers = {
        "user": user,
        "Content-Type": "application/json"
    }
    response = requests.get(url, params=params, headers = headers)
    if response.status_code == 200:
        data = response.json()
        return data['webSocket']['memberId'], data['webSocket']['websocket'], data['chat']['messages'], data['activeMembers']
    else:
        raise ValueError(f"Unable to reach servers with status {response.status_code}")

@dataclass
class LoginData:
    user_name: str
    channel: str
    
class Login(ModalScreen[LoginData]):
    DEFAULT_CSS = """

    Screen {
        align: center middle;
    }

    #login {
        padding: 0 1;
        width: 60;
        height: 15;
        border: thick $background 80%;
        background: $surface;
        align: center middle;
    }

    Button {
        width: 100%;
    }
    
    """
    def compose(self) -> ComposeResult:
        with Container(id = "login"):
            yield Label("User Name")
            yield Input(id = "username_input")
            yield Label("Channel Name")
            yield Input(id = "channel_input")
            yield Button("Connect")
            
    def on_button_pressed(self, event: Button.Pressed) -> None:
        username = self.query_one("#username_input").value
        channel = self.query_one("#channel_input").value
        self.dismiss(LoginData(user_name=username, channel=channel))



class ChatRoom(Screen):
    DEFAULT_CSS = """
    Screen {
        layout: grid;
        grid-size: 2;
        grid-rows: 1fr;
        grid-columns: 3fr 1fr;
        grid-gutter: 1;
        padding: 1 1
    }
    
    Input{

        dock: bottom;
    }
    RichLog{
        width: 100%;
        height: 100%
    }
    #members_container{
        margin-left: 5;
        padding: 1;
        width: 80%;
        border: heavy white
    }
    #chat_container{
        padding: 1 1;
        width: 100%;
        border: heavy white
    }
    """

    def __init__(self, name, user, channel, memberId, websocketUri, latestMessages, activeMembers, localhost):
        self.user = user
        self.channel = channel
        self.memberId = memberId
        self.websocketUri =  replace_ip_with_localhost(websocketUri) if localhost else websocketUri
        self.latestMessages = latestMessages
        self.active_members = activeMembers
        self.websocket = None
        super().__init__(name=name)

    def compose(self) -> ComposeResult:
            yield Header(self.name)
            yield Container(
                Input(),
                VerticalScroll(),
                RichLog(highlight=True, markup=True),
                id = "chat_container")
            yield Container(
                ListView(
                    ListItem(Label(self.user))
                    ),
                id = "members_container"
                )
            yield Footer()

    def format_message(self,message):
        itsMe = False
        if message['userName'] == self.user:
            itsMe = True
        return f"{format_date(message)} - {format_user_name(message, itsMe)}: {message['content']}"
    
    def _formatMember(self, userName):
        return ListItem(Label(f"[italic]{userName} - Me")) if userName == self.user else ListItem(Label(userName))
            
    def rewriteMembers(self):
        listView = self.query_one(ListView)
        listView.clear()
        members = []
        for member in self.active_members:
            members.append(self._formatMember(member['userName']))
        listView.extend(members)
        
    async def on_mount(self):
        # Prepopulate with latest messages
        self.rewriteMembers()
        self.websocket = await websockets.connect(self.websocketUri)

        log = self.query_one(RichLog)

        for message in self.latestMessages:
            log.write(self.format_message(message))
            
        self.run_worker(self.listen_for_messages())  # Start listening in the background


    async def send_message(self, message: str):
        # Send a message to the server
        log = self.query_one(RichLog)
        self.run_worker(self.websocket.send(message))
        self.query_one(Input).value = ""
        

    async def on_input_submitted(self, event: Input.Submitted) -> None:
        await self.send_message(event.value)

    async def listen_for_messages(self):
        log = self.query_one(RichLog)        
        log.write("[dim magenta]" + await self.websocket.recv())
        while True:
            message = json.loads(await self.websocket.recv())
            #log.write(message)
            if(message['message'] == "NEW_MESSAGE"):
                log.write(self.format_message(message))
            elif(message['message'] == "MEMBER_CONNECTED"):
                self.active_members.append({"name":  message['userName']})
                self.query_one(ListView).append(self._formatMember(message['userName']))
                if message['userName'] != self.user:
                    log.write("[dim white]" + message['userName'] + " connected!")
                    self.app.bell()
            elif(message['message'] == "MEMBER_DISCONNECTED"):
                element_to_remove = {"name": message['userName']}
                if element_to_remove in self.active_members:
                    self.active_members.remove(element_to_remove)
                    self.rewriteMembers()


    async def on_shutdown(self):
        # Close the WebSocket connection when the app shuts down
        await self.websocket.close()

    #async def on_load(self, event):
        #await asyncio.sleep(1)  # Give the app a second to load everything
        #self.query_one(Input).focus()

    async def on_key(self, event):
        if event.key == "q":
            await self.quit()
            
class ChatApp(App):
    def __init__(self, **kwargs):
        parser = argparse.ArgumentParser(description="A simple argument parser example.")
        # Add a positional argument
        #parser.add_argument("arg1", help="First positional argument")

        # Add an optional argument
        parser.add_argument("-l", "--localhost", help="If the client is being used locally or not", default=False)
        parser.add_argument("-u", "--connect_url", help="Url for the backend", default="http://localhost:8080/connect/")

        args = parser.parse_args()
        self.localhost = args.localhost
        self.connect_url = args.connect_url
        
        super().__init__(**kwargs)

    def createChat(self, login: LoginData):
        memberId, websocketUri, latestMessages, activeMembers = fetch_websocket_and_latest_messages(self.connect_url, login.channel, login.user_name)
        self.install_screen(ChatRoom(name = "Chat - " + login.channel, user = login.user_name, channel = login.channel, memberId=memberId, websocketUri=replace_ip_with_localhost(websocketUri), latestMessages = latestMessages, activeMembers=activeMembers, localhost = self.localhost), name="ChatRoom")
        self.push_screen("ChatRoom")

    #BINDINGS = [("b", "push_screen('ChatRoom')", "ChatRoom"), ("a", "push_screen('Login')", "Login")]
    
    def on_mount(self) -> None:
        self.install_screen(Login(), name="Login")
        self.push_screen("Login", self.createChat)  
        #self.createChat(LoginData("ruben", "236pbl"))

if __name__ == "__main__":
    app = ChatApp()
    app.run()


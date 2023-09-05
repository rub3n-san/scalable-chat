import asyncio
from textual.app import App
from textual.widgets import Placeholder, ScrollView, Text

class WSApp(App):

    async def on_load(self, event):
        await self.bind("q", "quit")

    async def on_startup(self, event):
        await self.view.dock(Placeholder(), edge="left", size=20)
        self.ws_view = ScrollView()
        await self.view.dock(self.ws_view, edge="right", size=80)

    async def receive_ws_message(self, ws_uri):
        async with websockets.connect(ws_uri) as websocket:
            while True:
                message = await websocket.recv()
                self.ws_view.update(Text(f"Received: {message}"))
                self.ws_view.update(Text(" "))
                await self.ws_view.refresh()

    async def on_mount(self, event):
        ws_uri = "ws://localhost:9091/connect/ola2?user=mae"  # Replace with your WebSocket URI
        asyncio.create_task(self.receive_ws_message(ws_uri))

WSApp.run()

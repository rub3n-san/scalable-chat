package ws.server.domain.services

import ws.server.infrastructure.databases.postgres.MetadataStore
import ws.server.infrastructure.databases.postgres.User

class UserService(val metadataStore: MetadataStore) {
    fun createUserIfDoesNotExists(userName: String): User {
        return metadataStore.findUser(userName) ?: createUser(userName)
    }
    
    private fun createUser(userName: String): User {
        val user = metadataStore.createUser(userName)
        println("User $userName created.")
        return user
    }

}
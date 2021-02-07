package club.eridani.qbotconsole.console
import kotlinx.serialization.Serializable

@Serializable
data class AccountStorage(val accounts: List<Account>) {
    @Serializable
    data class Account(val id: Long, val password: String)
}


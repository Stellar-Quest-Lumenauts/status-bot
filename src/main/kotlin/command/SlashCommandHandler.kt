package command

import command.cmd.RefetchCommand
import command.cmd.StatusCommand
import org.javacord.api.DiscordApi
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.ServerSlashCommandPermissionsBuilder
import org.javacord.api.interaction.SlashCommandPermissionType
import org.javacord.api.interaction.SlashCommandPermissions
import org.javacord.api.listener.interaction.SlashCommandCreateListener
import org.slf4j.LoggerFactory

class SlashCommandHandler(private val api: DiscordApi, private val commands: List<SlashCommand>) :
    SlashCommandCreateListener {

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        registerCommands()
        api.addSlashCommandCreateListener(this)
    }

    override fun onSlashCommandCreate(event: SlashCommandCreateEvent?) {
        if (event == null) return
        val interaction = event.slashCommandInteraction
        val cmd = commands.first { it.name == interaction.commandName }
        log.info("received /${cmd.name} from ${interaction.user.discriminatedName}" + if (interaction.server.isPresent) " on ${interaction.server.get().name}" else "")
        cmd.handle(interaction)
    }

    private fun registerCommands() {
        log.info("Telling Discord about slash commands")
        api.bulkOverwriteGlobalSlashCommands(
            commands.map { it.toSlashCommandBuilder() }
        ).join()

        val ownerId = 290464744794750976//DieKautz#3846
        val lumenaryGid = 836587722185375784
        val refetchCmdId = api.globalSlashCommands.join().first { it.name == RefetchCommand().name }.id
        val statusCmdId = api.globalSlashCommands.join().first { it.name == StatusCommand().name }.id
        api.servers.forEach { srv ->
            api.batchUpdateSlashCommandPermissions(
                srv, mutableListOf(
                    ServerSlashCommandPermissionsBuilder(
                        refetchCmdId,
                        listOf(
                            SlashCommandPermissions.create(ownerId, SlashCommandPermissionType.USER, true),
                            SlashCommandPermissions.create(lumenaryGid, SlashCommandPermissionType.ROLE, true)
                        )
                    ),
                    ServerSlashCommandPermissionsBuilder(
                        statusCmdId,
                        listOf(
                            SlashCommandPermissions.create(ownerId, SlashCommandPermissionType.USER, true),
                            SlashCommandPermissions.create(lumenaryGid, SlashCommandPermissionType.ROLE, true)
                        )
                    ),
                )
            )
        }
    }
}
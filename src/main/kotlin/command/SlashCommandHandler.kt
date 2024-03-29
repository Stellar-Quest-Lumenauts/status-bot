package command

import org.javacord.api.DiscordApi
import org.javacord.api.event.interaction.SlashCommandCreateEvent
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
        val slashCommandBuilders = commands.map { it.toSlashCommandBuilder() }.toMutableSet()

        api.bulkOverwriteGlobalApplicationCommands(slashCommandBuilders)
    }
}
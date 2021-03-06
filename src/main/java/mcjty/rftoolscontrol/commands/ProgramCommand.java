package mcjty.rftoolscontrol.commands;

import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.network.PacketItemNBTToServer;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;

/**
 * Client side command
 */
public class ProgramCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "rfctrl";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "rfctrl save | load";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            ItemStack item = Minecraft.getMinecraft().thePlayer.getHeldItemMainhand();
            if (item == null || !(item.getItem() instanceof ProgramCardItem)) {
                sender.addChatMessage(new TextComponentString(TextFormatting.RED + "You need a program card in your hand!"));
                return;
            }
            if ("save".equals(args[0])) {
                saveProgram(sender, args[1], item);
            } else if ("load".equals(args[0])) {
                loadProgram(sender, args[1], item);
            }
        } else {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Missing parameter (save <file> or load <file>)!"));
        }
    }

    private void loadProgram(ICommandSender sender, String arg, ItemStack item) {
//        File file = new File("." + File.separator + "rftoolscontrol" + File.separator + arg);
        File file = new File(arg);
        FileInputStream stream;
        String json;
        try {
            stream = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            stream.read(data);
            json = new String(data, "UTF-8");
        } catch (IOException e) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Error opening file for reading!"));
            return;
        }
        ProgramCardInstance program = ProgramCardInstance.readFromJson(json);
        program.writeToNBT(item);
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketItemNBTToServer(item.getTagCompound()));
        sender.addChatMessage(new TextComponentString("Loaded program!"));
    }

    private void saveProgram(ICommandSender sender, String arg, ItemStack item) {
        ProgramCardInstance program = ProgramCardInstance.parseInstance(item);
        String json = program.writeToJson();
//        File file = new File("." + File.separator + "rftoolscontrol" + File.separator + arg);
        File file = new File(arg);
        if (file.exists()) {
            file.delete();
        }
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Error opening file for writing!"));
            return;
        }
        writer.print(json);
        writer.close();
        sender.addChatMessage(new TextComponentString("Saved program!"));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length > 0) {
            return getListOfStringsMatchingLastWord(args, "save", "load");
        };
        return null;
    }
}

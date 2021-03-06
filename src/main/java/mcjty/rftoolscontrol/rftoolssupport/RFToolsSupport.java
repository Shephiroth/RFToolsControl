package mcjty.rftoolscontrol.rftoolssupport;

import com.google.common.base.Function;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.api.screens.IScreenModuleRegistry;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOp;
import mcjty.rftoolscontrol.logic.ParameterTools;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RFToolsSupport {

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(ModuleDataVariable.ID, buf -> {
                return new ModuleDataVariable(ParameterTools.readFromBuf(buf));
            });
            manager.registerModuleDataFactory(ModuleDataLog.ID, buf -> {
                return new ModuleDataLog(readLog(buf));
            });
            manager.registerModuleDataFactory(ModuleDataVectorArt.ID, buf -> {
                return new ModuleDataVectorArt(readGfxOp(buf));
            });
            return null;
        }
    }

    private static List<GfxOp> readGfxOp(ByteBuf buf) {
        int size = buf.readInt();
        if (size == 0) {
            return null;
        }
        List<GfxOp> operations = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            GfxOp op = GfxOp.readFromBuf(buf);
            operations.add(op);
        }

        return operations;
    }

    private static List<String> readLog(ByteBuf buf) {
        int size = buf.readInt();
        if (size == 0) {
            return null;
        }
        List<String> rc = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            rc.add(NetworkTools.readString(buf));
        }
        return rc;
    }

}

package ca.rttv.ecosystemic.entrypoints;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch(ModContainer mod) {
        MixinExtrasBootstrap.init();
    }
}

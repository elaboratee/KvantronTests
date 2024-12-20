import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme;
import gui.MainScreen;
import org.opencv.core.Core;

import javax.swing.*;

public class TestApp {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FlatGitHubDarkIJTheme.setup();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainScreen::showMainScreen);
    }
}

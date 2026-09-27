package com.aionemu.gameserver.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.GameServer.StartupHook;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.Race;

public class ZCXInfo {

        private static String VERSION = null;
        private static String MINOR = null;
        private static String COPYRIGHT = null;
        private static final Logger log = LoggerFactory.getLogger(ZCXInfo.class);
        private static int ELYOS_COUNT = 0;
        private static int ASMOS_COUNT = 0;
        private static double ELYOS_RATIO = 0.0;
        private static double ASMOS_RATIO = 0.0;
        private static final ReentrantLock lock = new ReentrantLock();

        public static void getInfo() throws IOException {
                readInfo();
                System.out.println("");
                System.out.println("   _____       __   __               _    ___              _   _ ");
                System.out.println("  / ___|  __ _ \\ \\ / /_   _  __ _   / \\  |_ _|  ___   _ __ | |_ | |");
                System.out.println("  \\___ \\ / _` |  \\ V /| | | |/ _` | / _ \\  | |  / _ \\ | '_ \\| __|| |");
                System.out.println("   ___) | (_| |   | | | |_| | (_| |/ ___ \\ | | | (_) || | | | |_ |_|");
                System.out.println("  |____/ \\__,_|   |_|  \\__,_|\\__, /_/   \\_\\___| \\___/ |_| |_|\\__|(_)");
                System.out.println("                           |___/                                     ");
                System.out.println("########  SaYberAioN - Private Aion 7.9 | Retail Clean + Custom  ########");
                System.out.println("########  Based on AionGerman-Core / AionLightning - Respect to authors  ########");
                System.out.println("");
                System.out.println("\t\t\tThanks to all who helped this project!");
                System.out.println("\t\t\tMajor Patch: " + getVersion());
                System.out.println("\t\t\tMinor Patch: " + getMinor());
                System.out.println("\t\t\tCopyright: " + getCopyright());
                System.out.println("");
        }

        public static void readInfo() throws IOException {
                FileReader fr = null;
                try {
                        fr = new FileReader("./config/info.txt");
                }
                catch (FileNotFoundException e) {
                        e.printStackTrace();
                }
                BufferedReader br = new BufferedReader(fr);
                setVersion(br.readLine());
                setMinor(br.readLine());
                setCopyright(br.readLine());
                br.close();
        }

        public static void setVersion(String version) { VERSION = version; }
        public static void setMinor(String minor) { MINOR = minor; }
        public static void setCopyright(String copyright) { COPYRIGHT = copyright; }
        public static String getVersion() { return VERSION; }
        public static String getMinor() { return MINOR; }
        public static String getCopyright() { return COPYRIGHT; }

        public static void checkForRatioLimitation() {
                if (GSConfig.ENABLE_RATIO_LIMITATION) {
                        GameServer.addStartupHook(new StartupHook() {
                                @Override
                                public void onStartup() {
                                        lock.lock();
                                        try {
                                                ASMOS_COUNT = DAOManager.getDAO(PlayerDAO.class).getCharacterCountForRace(Race.ASMODIANS);
                                                ELYOS_COUNT = DAOManager.getDAO(PlayerDAO.class).getCharacterCountForRace(Race.ELYOS);
                                                computeRatios();
                                        }
                                        catch (Exception e) {
                                                log.error("[Error] Something went wrong on checking ratio limitation");
                                                e.printStackTrace();
                                        }
                                        finally {
                                                lock.unlock();
                                        }
                                        displayRatios(false);
                                }
                        });
                }
        }

        public static void updateRatio(Race race, int i) {
                lock.lock();
                try {
                        switch (race) {
                                case ASMODIANS: ASMOS_COUNT += i; break;
                                case ELYOS: ELYOS_COUNT += i; break;
                                default: break;
                        }
                        computeRatios();
                }
                catch (Exception e) {
                        log.error("[Error] Cant update ratio limits");
                        e.printStackTrace();
                }
                finally {
                        lock.unlock();
                }
                displayRatios(true);
        }

        private static void computeRatios() {
                if ((ASMOS_COUNT <= GSConfig.RATIO_MIN_CHARACTERS_COUNT) && (ELYOS_COUNT <= GSConfig.RATIO_MIN_CHARACTERS_COUNT)) {
                        ASMOS_RATIO = ELYOS_RATIO = 50.0;
                }
                else {
                        ASMOS_RATIO = ASMOS_COUNT * 100.0 / (ASMOS_COUNT + ELYOS_COUNT);
                        ELYOS_RATIO = ELYOS_COUNT * 100.0 / (ASMOS_COUNT + ELYOS_COUNT);
                }
        }

        private static void displayRatios(boolean updated) {
                GameServer.log.info("[GameServer] Actual Factions Ratio " + (updated ? "updated " : "") + ": Elyos " + String.format("%.1f", ELYOS_RATIO) + " % - Asmodians " + String.format("%.1f", ASMOS_RATIO) + " %");
        }

        public static double getRatiosFor(Race race) {
                switch (race) {
                        case ASMODIANS: return ASMOS_RATIO;
                        case ELYOS: return ELYOS_RATIO;
                        default: return 0.0;
                }
        }

        public static int getCountFor(Race race) {
                switch (race) {
                        case ASMODIANS: return ASMOS_COUNT;
                        case ELYOS: return ELYOS_COUNT;
                        default: return 0;
                }
        }
}

package de.unierlangen.like;

import android.app.Application;
import android.content.Intent;

import com.better.wakelock.LogcatLogWriter;
import com.better.wakelock.Logger;
import com.better.wakelock.Logger.LogLevel;

import de.unierlangen.like.customviews.MapView;
import de.unierlangen.like.navigation.DijkstraRouter;
import de.unierlangen.like.navigation.Door;
import de.unierlangen.like.navigation.FileReader;
import de.unierlangen.like.navigation.MapBuilder;
import de.unierlangen.like.navigation.Navigation;
import de.unierlangen.like.navigation.Obstacle;
import de.unierlangen.like.navigation.RoomsDatabase;
import de.unierlangen.like.navigation.Tag;
import de.unierlangen.like.navigation.TagsDatabase;
import de.unierlangen.like.navigation.Wall;
import de.unierlangen.like.navigation.Zone;
import de.unierlangen.like.preferences.ReaderPreferencesFragment;
import de.unierlangen.like.rfid.GenericTag;
import de.unierlangen.like.rfid.Reader;
import de.unierlangen.like.rfid.ReaderIntents;
import de.unierlangen.like.rfid.ReaderService;
import de.unierlangen.like.serialport.Bluetooth;
import de.unierlangen.like.serialport.BluetoothStateMachine;
import de.unierlangen.like.serialport.CommunicationManager;
import de.unierlangen.like.serialport.Emulation;
import de.unierlangen.like.serialport.ProxyReceivingThead;
import de.unierlangen.like.serialport.ProxyTxChannel;
import de.unierlangen.like.serialport.ReadingThread;
import de.unierlangen.like.serialport.SerialPort;
import de.unierlangen.like.ui.AboutActivity;
import de.unierlangen.like.ui.ConsoleActivity;
import de.unierlangen.like.ui.FindRoomActivity;
import de.unierlangen.like.ui.HelpActivity;
import de.unierlangen.like.ui.MainYourLocationActivity;
import de.unierlangen.like.ui.OptionsMenuActivity;

public class NavigationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CommunicationManager.init(getApplicationContext());

        Logger logger = Logger.getDefaultLogger();
        logger.addLogWriter(new LogcatLogWriter());

        logger.setLogLevel(DijkstraRouter.class, LogLevel.WARN);
        logger.setLogLevel(Door.class, LogLevel.WARN);
        logger.setLogLevel(FileReader.class, LogLevel.WARN);
        logger.setLogLevel(MapBuilder.class, LogLevel.WARN);
        logger.setLogLevel(Navigation.class, LogLevel.WARN);
        logger.setLogLevel(Obstacle.class, LogLevel.WARN);
        logger.setLogLevel(RoomsDatabase.class, LogLevel.WARN);
        logger.setLogLevel(Tag.class, LogLevel.WARN);
        logger.setLogLevel(Wall.class, LogLevel.WARN);
        logger.setLogLevel(TagsDatabase.class, LogLevel.WARN);
        logger.setLogLevel(Zone.class, LogLevel.WARN);
        logger.setLogLevel(GenericTag.class, LogLevel.WARN);
        logger.setLogLevel(Reader.class, LogLevel.WARN);
        logger.setLogLevel(TagsDatabase.class, LogLevel.WARN);
        logger.setLogLevel(ReaderService.class, LogLevel.WARN);

        logger.setLogLevel(MapView.class, LogLevel.WARN);

        logger.setLogLevel(Bluetooth.class, LogLevel.WARN);
        logger.setLogLevel(BluetoothStateMachine.class, LogLevel.WARN);
        logger.setLogLevel(CommunicationManager.class, LogLevel.WARN);
        logger.setLogLevel(Emulation.class, LogLevel.WARN);
        logger.setLogLevel(ProxyReceivingThead.class, LogLevel.WARN);
        logger.setLogLevel(ProxyTxChannel.class, LogLevel.WARN);
        logger.setLogLevel(ReadingThread.class, LogLevel.WARN);
        logger.setLogLevel(SerialPort.class, LogLevel.WARN);

        logger.setLogLevel(AboutActivity.class, LogLevel.WARN);
        logger.setLogLevel(CommunicationManager.class, LogLevel.WARN);
        logger.setLogLevel(ConsoleActivity.class, LogLevel.WARN);
        logger.setLogLevel(FindRoomActivity.class, LogLevel.DEBUG);
        logger.setLogLevel(HelpActivity.class, LogLevel.WARN);
        logger.setLogLevel(MainYourLocationActivity.class, LogLevel.DEBUG);
        logger.setLogLevel(OptionsMenuActivity.class, LogLevel.WARN);
        logger.setLogLevel(ReaderPreferencesFragment.class, LogLevel.WARN);

        Intent intent = new Intent(this, ReaderService.class);
        intent.setAction(ReaderIntents.ACTION_READ_TAGS);
        startService(intent);
    }
}

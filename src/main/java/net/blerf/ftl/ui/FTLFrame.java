package net.blerf.ftl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import net.vhati.ftldat.FTLDat;
import net.vhati.modmanager.core.FTLUtilities;

import net.blerf.ftl.model.Profile;
import net.blerf.ftl.parser.DataManager;
import net.blerf.ftl.parser.MysteryBytes;
import net.blerf.ftl.parser.ProfileParser;
import net.blerf.ftl.parser.SavedGameParser;
import net.blerf.ftl.ui.DumpPanel;
import net.blerf.ftl.ui.ExtensionFileFilter;
import net.blerf.ftl.ui.HTMLEditorTransferHandler;
import net.blerf.ftl.ui.ProfileGeneralAchievementsPanel;
import net.blerf.ftl.ui.ProfileGeneralStatsPanel;
import net.blerf.ftl.ui.ProfileShipStatsPanel;
import net.blerf.ftl.ui.ProfileShipUnlockPanel;
import net.blerf.ftl.ui.SavedGameFloorplanPanel;
import net.blerf.ftl.ui.SavedGameGeneralPanel;
import net.blerf.ftl.ui.SavedGameHangarPanel;
import net.blerf.ftl.ui.SavedGameSectorMapPanel;
import net.blerf.ftl.ui.SavedGameSectorTreePanel;
import net.blerf.ftl.ui.SavedGameStateVarsPanel;
import net.blerf.ftl.ui.StatusbarMouseListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FTLFrame extends JFrame {

	private static final Logger log = LogManager.getLogger(FTLFrame.class);

	private static final String PROFILE_SHIP_UNLOCK = "Ship Unlocks & Achievements";
	private static final String PROFILE_GENERAL_ACH = "General Achievements";
	private static final String PROFILE_GENERAL_STATS = "General Stats";
	private static final String PROFILE_SHIP_STATS = "Ship Stats";
	private static final String PROFILE_DUMP = "Dump";

	private static final String SAVE_DUMP = "Dump";
	private static final String SAVE_GENERAL = "General";
	private static final String SAVE_PLAYER_SHIP = "Player Ship";
	private static final String SAVE_NEARBY_SHIP = "Nearby Ship";
	private static final String SAVE_CHANGE_SHIP = "Change Ship";
	private static final String SAVE_SECTOR_MAP = "Sector Map";
	private static final String SAVE_SECTOR_TREE = "Sector Tree";
	private static final String SAVE_STATE_VARS = "State Vars";

	private Profile stockProfile = null;
	private Profile profile = null;
	private SavedGameParser.SavedGameState gameState = null;

	private ImageIcon openIcon = new ImageIcon( ClassLoader.getSystemResource("open.gif") );
	private ImageIcon saveIcon = new ImageIcon( ClassLoader.getSystemResource("save.gif") );
	private ImageIcon unlockIcon = new ImageIcon( ClassLoader.getSystemResource("unlock.png") );
	private ImageIcon aboutIcon = new ImageIcon( ClassLoader.getSystemResource("about.gif") );
	private ImageIcon updateIcon = new ImageIcon( ClassLoader.getSystemResource("update.gif") );
	private ImageIcon releaseNotesIcon = new ImageIcon( ClassLoader.getSystemResource("release-notes.png") );

	private URL aboutPage = ClassLoader.getSystemResource("about.html");
	private URL latestVersionTemplate = ClassLoader.getSystemResource("update.html");
	private URL releaseNotesTemplate = ClassLoader.getSystemResource("release-notes.html");

	private String latestVersionUrl = "https://raw.github.com/Vhati/ftl-profile-editor/master/latest-version.txt";
	private String versionHistoryUrl = "https://raw.github.com/Vhati/ftl-profile-editor/master/release-notes.txt";
	private String bugReportUrl = "https://github.com/Vhati/ftl-profile-editor/issues/new";
	private String forumThreadUrl = "http://www.ftlgame.com/forum/viewtopic.php?f=7&t=10959";

	private ArrayList<JButton> updatesButtonList = new ArrayList<JButton>();
	private Runnable updatesCallback;

	private JTabbedPane profileTabsPane;
	private ProfileShipUnlockPanel profileShipUnlockPanel;
	private ProfileGeneralAchievementsPanel profileGeneralAchsPanel;
	private ProfileGeneralStatsPanel profileGeneralStatsPanel;
	private ProfileShipStatsPanel profileShipStatsPanel;
	private DumpPanel profileDumpPanel;

	private JButton gameStateSaveBtn;
	private JTabbedPane savedGameTabsPane;
	private DumpPanel savedGameDumpPanel;
	private SavedGameGeneralPanel savedGameGeneralPanel;
	private SavedGameFloorplanPanel savedGamePlayerFloorplanPanel;
	private SavedGameFloorplanPanel savedGameNearbyFloorplanPanel;
	private SavedGameHangarPanel savedGameHangarPanel;
	private SavedGameSectorMapPanel savedGameSectorMapPanel;
	private SavedGameSectorTreePanel savedGameSectorTreePanel;
	private SavedGameStateVarsPanel savedGameStateVarsPanel;
	private JLabel statusLbl;
	private final HyperlinkListener linkListener;

	private String appName;
	private int appVersion;


	public FTLFrame( String appName, int appVersion ) {
		this.appName = appName;
		this.appVersion = appVersion;

		// GUI setup
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		setSize( 800, 700 );
		setLocationRelativeTo( null );
		setTitle( String.format( "%s v%d", appName, appVersion ) );

		try {
			setIconImage( ImageIO.read( ClassLoader.getSystemResource("unlock.png") ) );
		}
		catch ( IOException e ) {
			log.error( "Error reading \"unlock.png\".", e );
		}

		linkListener = new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate( HyperlinkEvent e ) {
				if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
					log.trace( "Dialog link clicked: "+ e.getURL() );

					if ( Desktop.isDesktopSupported() ) {
						try {
							Desktop.getDesktop().browse( e.getURL().toURI() );
							log.trace( "Link opened in external browser." );
						}
						catch ( Exception f ) {
							log.error( "Unable to open link.", f );
						}
					}
				}
			}
		};

		initCheckboxIcons();

		JPanel contentPane = new JPanel( new BorderLayout() );
		setContentPane(contentPane);

		JTabbedPane tasksPane = new JTabbedPane();
		contentPane.add( tasksPane, BorderLayout.CENTER );

		JPanel profilePane = new JPanel( new BorderLayout() );
		tasksPane.addTab( "Profile", profilePane );

		JToolBar profileToolbar = new JToolBar();
		setupProfileToolbar(profileToolbar);
		profilePane.add(profileToolbar, BorderLayout.NORTH);

		profileTabsPane = new JTabbedPane();
		profilePane.add( profileTabsPane, BorderLayout.CENTER );

		profileShipUnlockPanel = new ProfileShipUnlockPanel(this);
		profileGeneralAchsPanel = new ProfileGeneralAchievementsPanel(this);
		profileGeneralStatsPanel = new ProfileGeneralStatsPanel(this);
		profileShipStatsPanel = new ProfileShipStatsPanel(this);
		profileDumpPanel = new DumpPanel();

		JScrollPane profileShipUnlockScroll = new JScrollPane( profileShipUnlockPanel );
		profileShipUnlockScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane profileGeneralAchsScroll = new JScrollPane( profileGeneralAchsPanel );
		profileGeneralAchsScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane profileGeneralStatsScroll = new JScrollPane( profileGeneralStatsPanel );
		profileGeneralStatsScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane profileShipStatsScroll = new JScrollPane( profileShipStatsPanel );
		profileShipStatsScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		profileTabsPane.addTab( PROFILE_SHIP_UNLOCK, profileShipUnlockScroll );
		profileTabsPane.addTab( PROFILE_GENERAL_ACH, profileGeneralAchsScroll );
		profileTabsPane.addTab( PROFILE_GENERAL_STATS, profileGeneralStatsScroll );
		profileTabsPane.addTab( PROFILE_SHIP_STATS, profileShipStatsScroll );
		profileTabsPane.addTab( PROFILE_DUMP, profileDumpPanel );


		JPanel savedGamePane = new JPanel( new BorderLayout() );
		tasksPane.addTab( "Saved Game", savedGamePane );

		JToolBar savedGameToolbar = new JToolBar();
		setupSavedGameToolbar(savedGameToolbar);
		savedGamePane.add(savedGameToolbar, BorderLayout.NORTH);

		savedGameTabsPane = new JTabbedPane();
		savedGamePane.add( savedGameTabsPane, BorderLayout.CENTER );

		savedGameDumpPanel = new DumpPanel();
		savedGameGeneralPanel = new SavedGameGeneralPanel(this);
		savedGamePlayerFloorplanPanel = new SavedGameFloorplanPanel(this);
		savedGameNearbyFloorplanPanel = new SavedGameFloorplanPanel(this);
		savedGameHangarPanel = new SavedGameHangarPanel(this);
		savedGameSectorMapPanel = new SavedGameSectorMapPanel(this);
		savedGameSectorTreePanel = new SavedGameSectorTreePanel(this);
		savedGameStateVarsPanel = new SavedGameStateVarsPanel(this);

		JScrollPane savedGameGeneralScroll = new JScrollPane( savedGameGeneralPanel );
		savedGameGeneralScroll.getVerticalScrollBar().setUnitIncrement( 14 );

		JScrollPane savedGameSectorTreeScroll = new JScrollPane( savedGameSectorTreePanel );
		savedGameSectorTreeScroll.getVerticalScrollBar().setUnitIncrement( 14 );
		savedGameSectorTreeScroll.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

		savedGameTabsPane.addTab( SAVE_DUMP, savedGameDumpPanel );
		savedGameTabsPane.addTab( SAVE_GENERAL, savedGameGeneralScroll );
		savedGameTabsPane.addTab( SAVE_PLAYER_SHIP, savedGamePlayerFloorplanPanel );
		savedGameTabsPane.addTab( SAVE_NEARBY_SHIP, savedGameNearbyFloorplanPanel );
		savedGameTabsPane.addTab( SAVE_CHANGE_SHIP, savedGameHangarPanel );
		savedGameTabsPane.addTab( SAVE_SECTOR_MAP, savedGameSectorMapPanel );
		savedGameTabsPane.addTab( SAVE_SECTOR_TREE, savedGameSectorTreeScroll );
		savedGameTabsPane.addTab( SAVE_STATE_VARS, savedGameStateVarsPanel );

		JPanel statusPanel = new JPanel();
		statusPanel.setLayout( new BoxLayout(statusPanel, BoxLayout.Y_AXIS) );
		statusPanel.setBorder( BorderFactory.createLoweredBevelBorder() );
		statusLbl = new JLabel( " " );
		//statusLbl.setFont( statusLbl.getFont().deriveFont(Font.PLAIN) );
		statusLbl.setBorder( BorderFactory.createEmptyBorder(2, 4, 2, 4) );
		statusLbl.setAlignmentX( Component.LEFT_ALIGNMENT );
		statusPanel.add( statusLbl );
		contentPane.add( statusPanel, BorderLayout.SOUTH );

		// Load blank profile (sets Kestrel unlock).
		stockProfile = Profile.createEmptyProfile();
		loadProfile( stockProfile );

		loadGameState( null );

		// Check for updates in a seperate thread.
		setStatusText( "Checking for updates..." );
		Thread t = new Thread( "CheckVersion" ) {
			@Override
			public void run() {
				checkForUpdate();
			}
		};
		t.setDaemon(true);
		t.start();
	}

	private void showErrorDialog( String message ) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void initCheckboxIcons() {
		log.trace( "Initialising standard cycle box stuff." );

		InputStream stream = null;
		try {
			stream = DataManager.get().getResourceInputStream( "img/customizeUI/box_lock_on.png" );
			ImageUtilities.setLockImage( ImageIO.read( stream ) );
		}
		catch ( IOException e ) {
			log.error( "Error reading lock image." , e );
		}
		finally {
			try {if ( stream != null ) stream.close();}
			catch ( IOException e ) {}
		}
	}

	private void setupProfileToolbar( JToolBar toolbar ) {
		log.trace( "Initialising Profile toolbar." );

		toolbar.setMargin( new Insets(5, 5, 5, 5) );
		toolbar.setFloatable(false);

		final JFileChooser fc = new JFileChooser();
		fc.setFileHidingEnabled( false );
		fc.addChoosableFileFilter( new FileFilter() {
			@Override
			public String getDescription() {
				return "FTL Profile (ae_prof.sav; prof.sav)";
			}
			@Override
			public boolean accept(File f) {
				if ( f.isDirectory() ) return true;
				if ( f.getName().equalsIgnoreCase("ae_prof.sav") ) return true;
				if ( f.getName().equalsIgnoreCase("prof.sav") ) return true;
				return false;
			}
		});

		final File candidateAEProfileFile = new File( FTLUtilities.findUserDataDir(), "ae_prof.sav" );
		final File candidateClassicProfileFile = new File( FTLUtilities.findUserDataDir(), "prof.sav" );
		final File userDataDir = FTLUtilities.findUserDataDir();
		if ( candidateAEProfileFile.exists() ) {
			fc.setSelectedFile( candidateAEProfileFile );
		}
		else if ( candidateClassicProfileFile.exists() ) {
			fc.setSelectedFile( candidateClassicProfileFile );
		}
		else {
			fc.setCurrentDirectory( userDataDir );
		}

		fc.setMultiSelectionEnabled(false);

		JButton profileOpenBtn = new JButton( "Open", openIcon );
		profileOpenBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Open profile button clicked." );

				fc.setDialogTitle( "Open Profile" );
				int chooserResponse = fc.showOpenDialog(FTLFrame.this);
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "continue.sav".equals(chosenFile.getName()) ) {
						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the Profile tab, and you're opening \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileInputStream in = null;
					StringBuilder hexBuf = new StringBuilder();
					boolean hashFailed = false;
					Exception exception = null;

					try {
						log.info( "Opening profile: "+ chosenFile.getAbsolutePath() );

						in = new FileInputStream( chosenFile );

						// Hash whole file, then go back to the beginning.
						String readHash = FTLDat.calcStreamMD5( in );
						in.getChannel().position( 0 );

						// Read the content in advance, in case an error ocurs.
						byte[] buf = new byte[4096];
						int len = 0;
						while ( (len = in.read(buf)) >= 0 ) {
							for (int j=0; j < len; j++) {
								hexBuf.append( String.format( "%02x", buf[j] ) );
								if ( (j+1) % 32 == 0 ) {
									hexBuf.append( "\n" );
								}
							}
						}
						in.getChannel().position( 0 );

						// Parse file data.
						ProfileParser parser = new ProfileParser();
						Profile p = parser.readProfile(in);
						log.trace( "Profile read successfully." );

						Profile mockProfile = new Profile( p );
						FTLFrame.this.loadProfile( mockProfile );

						// Perform mock write.
						// The update() incidentally triggers load() of the modified profile.
						ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
						FTLFrame.this.updateProfile( mockProfile );
						parser.writeProfile( mockOut, mockProfile );
						mockOut.close();

						// Hash result.
						ByteArrayInputStream mockIn = new ByteArrayInputStream( mockOut.toByteArray() );
						String writeHash = FTLDat.calcStreamMD5( mockIn );
						mockIn.close();

						// Compare hashes.
						if ( !writeHash.equals( readHash ) ) {
							log.error( "Hashes did not match after a mock write. Unable to assure valid parsing." );
							hashFailed = true;
						}

						// Reload the original unmodified profile.
						FTLFrame.this.loadProfile( p );
					}
					catch( Exception f ) {
						log.error( String.format("Error reading profile (\"%s\").", chosenFile.getName()), f );
						showErrorDialog( String.format("Error reading profile (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage()) );
						exception = f;
					}
					finally {
						try {if ( in != null ) in.close();}
						catch ( IOException f ) {}
					}

					if ( hashFailed || exception != null ) {
						if ( hexBuf.length() > 0 ) {
							StringBuilder errBuf = new StringBuilder();

							if ( hashFailed && exception == null ) {
								errBuf.append( "Your profile loaded, but re-saving will not create an identical file.<br/>");
								errBuf.append( "You CAN technically proceed anyway, but there is risk of corruption.<br/>" );
							}
							else {
								errBuf.append( "Your profile could not be interpreted correctly.<br/>" );
							}

							errBuf.append( "<br/>" );
							errBuf.append( "To submit a bug report, you can use <a href='"+ bugReportUrl +"'>GitHub</a> (Signup is free).<br/>" );
							errBuf.append( "Or post to the FTL forums <a href='"+ forumThreadUrl +"'>here</a> (Signup there is also free).<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "On GitHub, set the issue title as \"Profile Parser Error\".<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "I will fix the problem and release a new version as soon as I can.<br/>" );
							errBuf.append( "<br/><br/>" );
							errBuf.append( "Copy (Ctrl-A, Ctrl-C) the following text, including \"[ code ] tags\"." );
							errBuf.append( "<br/><br/>" );

							StringBuilder reportBuf = new StringBuilder();
							reportBuf.append( "[code]\n" );
							reportBuf.append( "Profile Parser Error\n" );
							reportBuf.append( "\n" );

							if ( hashFailed ) {
								reportBuf.append( "Hashes did not match after a mock write.\n" );
								reportBuf.append( "\n" );
							}

							if ( exception != null ) {
								reportBuf.append( String.format("Exception: %s\n", exception.toString()) );
								reportBuf.append( "\n" );

								reportBuf.append( "Stack Trace...\n" );
								StackTraceElement[] traceElements = exception.getStackTrace();
								int traceDepth = 5;
								for (int i=0; i < traceDepth && i < traceElements.length; i++) {
									reportBuf.append( String.format("  %s\n", traceElements[i].toString()) );
								}
								reportBuf.append( "\n" );
							}

							reportBuf.append( String.format("Editor Version: %s\n", appVersion) );
							reportBuf.append( String.format("OS: %s %s\n", System.getProperty("os.name"), System.getProperty("os.version")) );
							reportBuf.append( String.format("VM: %s, %s, %s\n", System.getProperty("java.vm.name"), System.getProperty("java.version"), System.getProperty("os.arch")) );
							reportBuf.append( "[/code]\n" );
							reportBuf.append( "\n" );
							reportBuf.append( String.format("File (\"%s\")...\n", chosenFile.getName()) );
							reportBuf.append( "[code]\n" );
							reportBuf.append( hexBuf );
							reportBuf.append( "\n[/code]\n" );

							JDialog failDialog = createBugReportDialog( "Profile Parser Error", errBuf.toString(), reportBuf.toString() );
							failDialog.setVisible(true);
						}
					}
				}
				else {
					log.trace( "Open dialog cancelled." );
				}
			}
		});
		profileOpenBtn.addMouseListener( new StatusbarMouseListener(this, "Open an existing profile.") );
		toolbar.add( profileOpenBtn );

		JButton profileSaveBtn = new JButton( "Save", saveIcon );
		profileSaveBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Save profile button clicked." );

				if ( profile == stockProfile ) {
					int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting might be a mistake.\n\nThis is the blank default profile, which the editor uses for eye candy.\nNormally one would OPEN an existing profile first.\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
					if ( sillyResponse != JOptionPane.YES_OPTION ) return;

					fc.setSelectedFile( candidateClassicProfileFile );  // The stock profile is a "prof.sav".
				}

				fc.setDialogTitle( "Save Profile" );
				int chooserResponse = fc.showSaveDialog(FTLFrame.this);
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "continue.sav".equals(chosenFile.getName()) ) {
						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the Profile tab, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}

					if ( !sillyMistake && profile.getHeaderAlpha() == 4 &&
					     "ae_prof.sav".equals(chosenFile.getName()) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is NOT an AE profile, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}

					if ( !sillyMistake && profile.getHeaderAlpha() == 9 &&
					     "prof.sav".equals(chosenFile.getName()) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is an AE profile, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileOutputStream out = null;
					try {
						log.info( "Saving profile: "+ chosenFile.getAbsolutePath() );

						if ( chosenFile.exists() ) {
							String bakName = chosenFile.getName() +".bak";
							File bakFile = new File( chosenFile.getParentFile(), bakName );
							boolean bakValid = true;

							if ( bakFile.exists() ) {
								bakValid = bakFile.delete();
								if ( !bakValid ) log.warn( "Profile will be overwritten. Could not delete existing backup: "+ bakName );
							}
							if ( bakValid ) {
								bakValid = chosenFile.renameTo( bakFile );
								if ( !bakValid ) log.warn( "Profile will be overwritten. Could not rename existing file: "+ chosenFile.getName() );
							}

							if ( bakValid ) {
								log.info( "Profile was backed up: "+ bakName );
							}
						}

						out = new FileOutputStream( chosenFile );

						ProfileParser parser = new ProfileParser();
						FTLFrame.this.updateProfile( profile );
						parser.writeProfile( out, profile );
					}
					catch( IOException f ) {
						log.error( String.format("Error saving profile (\"%s\").", chosenFile.getName()), f );
						showErrorDialog( String.format("Error saving profile (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage()) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
				else {
					log.trace( "Save dialog cancelled." );
				}
			}
		});
		profileSaveBtn.addMouseListener( new StatusbarMouseListener(this, "Save the current profile.") );
		toolbar.add( profileSaveBtn );

		JButton profileDumpBtn = new JButton( "Dump", saveIcon );
		profileDumpBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Dump profile button clicked." );

				if ( profile == null ) return;

				JFileChooser dumpChooser = new JFileChooser();
				dumpChooser.setCurrentDirectory( fc.getCurrentDirectory() );
				dumpChooser.setFileHidingEnabled( false );

				ExtensionFileFilter txtFilter = new ExtensionFileFilter( "Text Files (*.txt)", new String[] {".txt"} );
				dumpChooser.addChoosableFileFilter( txtFilter );

				int chooserResponse = dumpChooser.showSaveDialog(FTLFrame.this);
				File chosenFile = dumpChooser.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( !chosenFile.exists() && dumpChooser.getFileFilter() == txtFilter && !txtFilter.accept(chosenFile) ) {
						chosenFile = new File( chosenFile.getAbsolutePath() + txtFilter.getPrimarySuffix() );
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "ae_prof.sav".equals(chosenFile.getName()) ||
					     "prof.sav".equals(chosenFile.getName()) ||
					     "continue.sav".equals(chosenFile.getName()) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nYou're dumping a text summary called \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					BufferedWriter out = null;
					try {
						log.info( "Dumping profile: "+ chosenFile.getAbsolutePath() );

						out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( chosenFile ) ) );
						out.write( profile.toString() );
						out.close();
					}
					catch( IOException f ) {
						log.error( String.format("Error dumping profile (\"%s\").", chosenFile.getName()), f );
						showErrorDialog( String.format("Error dumping profile (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage()) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
				else {
					log.trace( "Dump dialog cancelled." );
				}
			}
		});
		profileDumpBtn.addMouseListener( new StatusbarMouseListener(this, "Dump unmodified profile info to a text file.") );
		toolbar.add( profileDumpBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton profileUnlockShipsBtn = new JButton( "Unlock All Ships", unlockIcon );
		profileUnlockShipsBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Unlock all ships button clicked." );
				profileShipUnlockPanel.unlockAllShips();
			}
		});
		profileUnlockShipsBtn.addMouseListener( new StatusbarMouseListener(this, "Unlock All Ships (except Type-B).") );
		toolbar.add( profileUnlockShipsBtn );


		JButton profileUnlockShipAchsBtn = new JButton( "Unlock All Ship Achievements", unlockIcon );
		profileUnlockShipAchsBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Unlock all ship achievements button clicked." );
				profileShipUnlockPanel.unlockAllShipAchievements();
			}
		});
		profileUnlockShipAchsBtn.addMouseListener( new StatusbarMouseListener(this, "Unlock All Ship Achievements (and Type-B ships).") );
		toolbar.add( profileUnlockShipAchsBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton profileExtractBtn = new JButton( "Extract Dats", saveIcon );
		profileExtractBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Extract button clicked." );

				JFileChooser extractChooser = new JFileChooser();
				extractChooser.setDialogTitle( "Choose a dir to extract into" );
				extractChooser.setFileHidingEnabled( false );
				extractChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				extractChooser.setMultiSelectionEnabled(false);

				if ( extractChooser.showSaveDialog(FTLFrame.this) == JFileChooser.APPROVE_OPTION ) {
					try {
						File extractDir = extractChooser.getSelectedFile();
						log.trace( "Extracting resources into dir: "+ extractDir.getAbsolutePath() );

						JOptionPane.showMessageDialog( FTLFrame.this, "This may take a few seconds.\nClick OK to proceed.", "About to Extract", JOptionPane.PLAIN_MESSAGE );

						DataManager.get().extractDataDat( extractDir );
						DataManager.get().extractResourceDat( extractDir );

						JOptionPane.showMessageDialog( FTLFrame.this, "All dat content extracted successfully.", "Extraction Complete", JOptionPane.PLAIN_MESSAGE );
					}
					catch( IOException f ) {
						log.error( "Error extracting dats.", f );
						showErrorDialog( String.format("Error extracting dats:\n%s: %s", f.getClass().getSimpleName(), f.getMessage()) );
					}
				}
				else {
					log.trace( "Extract dialog cancelled." );
				}
			}
		});
		profileExtractBtn.addMouseListener( new StatusbarMouseListener(this, "Extract dat content to a directory.") );
		toolbar.add( profileExtractBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton profileAboutBtn = createAboutButton();
		toolbar.add( profileAboutBtn );

		JButton profileUpdatesBtn = createUpdatesButton();
		updatesButtonList.add( profileUpdatesBtn );
		toolbar.add( profileUpdatesBtn );
	}

	private void setupSavedGameToolbar( JToolBar toolbar ) {
		log.trace( "Initialising SavedGame toolbar." );

		toolbar.setMargin( new Insets(5, 5, 5, 5) );
		toolbar.setFloatable(false);

		final JFileChooser fc = new JFileChooser();
		fc.setFileHidingEnabled( false );
		fc.addChoosableFileFilter( new FileFilter() {
			@Override
			public String getDescription() {
				return "FTL Saved Game (continue.sav)";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().equalsIgnoreCase("continue.sav");
			}
		});

		File candidateSaveFile = new File( FTLUtilities.findUserDataDir(), "continue.sav" );
		if ( candidateSaveFile.exists() ) {
			fc.setSelectedFile( candidateSaveFile );
		} else {
			fc.setCurrentDirectory( FTLUtilities.findUserDataDir() );
		}

		fc.setMultiSelectionEnabled(false);

		JButton gameStateOpenBtn = new JButton( "Open", openIcon );
		gameStateOpenBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Open saved game button clicked." );

				fc.setDialogTitle( "Open Saved Game" );
				int chooserResponse = fc.showOpenDialog(FTLFrame.this);
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "ae_prof.sav".equals(chosenFile.getName()) ||
					     "prof.sav".equals(chosenFile.getName()) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the SavedGame tab, and you're opening \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileInputStream in = null;
					StringBuilder hexBuf = new StringBuilder();
					Exception exception = null;

					try {
						log.info( "Opening game state: "+ chosenFile.getAbsolutePath() );

						in = new FileInputStream( chosenFile );

						// Read the content in advance, in case an error ocurs.
						byte[] buf = new byte[4096];
						int len = 0;
						while ( (len = in.read(buf)) >= 0 ) {
							for (int j=0; j < len; j++) {
								hexBuf.append( String.format( "%02x", buf[j] ) );
								if ( (j+1) % 32 == 0 ) {
									hexBuf.append( "\n" );
								}
							}
						}
						in.getChannel().position( 0 );

						SavedGameParser parser = new SavedGameParser();
						SavedGameParser.SavedGameState gs = parser.readSavedGame( in );
						loadGameState( gs );

						log.trace( "Game state read successfully." );

						if ( gameState.getMysteryList().size() > 0 ) {
							StringBuilder musteryBuf = new StringBuilder();
							musteryBuf.append( "This saved game file contains mystery bytes the developers hadn't anticipated!\n" );
							boolean first = true;
							for (MysteryBytes m : gameState.getMysteryList()) {
								if (first) { first = false; }
								else { musteryBuf.append( ",\n" ); }
								musteryBuf.append(m.toString().replaceAll( "(^|\n)(.+)", "$1  $2") );
							}
							log.warn( musteryBuf.toString() );
						}
					}
					catch( Exception f ) {
						log.error( String.format("Error reading saved game (\"%s\").", chosenFile.getName()), f );
						showErrorDialog( String.format("Error reading saved game (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage()) );
						exception = f;
					}
					finally {
						try {if ( in != null ) in.close();}
						catch ( IOException f ) {}
					}

					if ( exception != null ) {
						if ( hexBuf.length() > 0 ) {
							StringBuilder errBuf = new StringBuilder();
							errBuf.append( "Your saved game could not be interpreted correctly.<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "To submit a bug report, you can use <a href='"+ bugReportUrl +"'>GitHub</a> (Signup is free).<br/>");
							errBuf.append( "Or post to the FTL forums <a href='"+ forumThreadUrl +"'>here</a> (Signup there is also free).<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "On GitHub, set the issue title as \"SavedGame Parser Error\".<br/>" );
							errBuf.append( "<br/>" );
							errBuf.append( "I will fix the problem and release a new version as soon as I can.<br/>" );
							errBuf.append( "<br/><br/>" );
							errBuf.append( "Copy (Ctrl-A, Ctrl-C) the following text, including \"[ code ] tags\"." );
							errBuf.append( "<br/><br/>" );

							StringBuilder reportBuf = new StringBuilder();
							reportBuf.append( "[code]\n" );
							reportBuf.append( "SavedGame Parser Error\n" );
							reportBuf.append( "\n" );

							if ( exception != null ) {
								reportBuf.append( String.format("Exception: %s\n", exception.toString()) );
								reportBuf.append( "\n" );

								reportBuf.append( "Stack Trace...\n" );
								StackTraceElement[] traceElements = exception.getStackTrace();
								int traceDepth = 5;
								for (int i=0; i < traceDepth && i < traceElements.length; i++) {
									reportBuf.append( String.format("  %s\n", traceElements[i].toString()) );
								}
								reportBuf.append( "\n" );
							}

							reportBuf.append( String.format("Editor Version: %s\n", appVersion) );
							reportBuf.append( String.format("OS: %s %s\n", System.getProperty("os.name"), System.getProperty("os.version")) );
							reportBuf.append( String.format("VM: %s, %s, %s\n", System.getProperty("java.vm.name"), System.getProperty("java.version"), System.getProperty("os.arch")) );
							reportBuf.append( "[/code]\n" );
							reportBuf.append( "\n" );
							reportBuf.append( String.format("File (\"%s\")...\n", chosenFile.getName()) );
							reportBuf.append( "[code]\n" );
							reportBuf.append( hexBuf );
							reportBuf.append( "\n[/code]\n" );

							JDialog failDialog = createBugReportDialog( "SavedGame Parser Error", errBuf.toString(), reportBuf.toString() );
							failDialog.setVisible(true);
						}
					}
				}
				else {
					log.trace( "Open dialog cancelled." );
				}
			}
		});
		gameStateOpenBtn.addMouseListener( new StatusbarMouseListener(this, "Open an existing saved game.") );
		toolbar.add( gameStateOpenBtn );

		gameStateSaveBtn = new JButton( "Save", saveIcon );
		gameStateSaveBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Save game state button clicked." );

				if ( gameState == null ) return;

				if ( gameState.getMysteryList().size() > 0 )
					log.warn( "The original saved game file contained mystery bytes, which will be omitted in the new file." );

				fc.setDialogTitle( "Save Game State" );
				int chooserResponse = fc.showSaveDialog(FTLFrame.this);
				File chosenFile = fc.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "ae_prof.sav".equals(chosenFile.getName()) ||
					     "prof.sav".equals(chosenFile.getName()) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nThis is the SavedGame tab, and you're saving \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					FileOutputStream out = null;
					try {
						log.info( "Saving game state: "+ chosenFile.getAbsolutePath() );

						if ( chosenFile.exists() ) {
							String bakName = chosenFile.getName() +".bak";
							File bakFile = new File( chosenFile.getParentFile(), bakName );
							boolean bakValid = true;

							if ( bakFile.exists() ) {
								bakValid = bakFile.delete();
								if ( !bakValid ) log.warn( "Saved game will be overwritten. Could not delete existing backup: "+ bakName );
							}
							if ( bakValid ) {
								bakValid = chosenFile.renameTo( bakFile );
								if ( !bakValid ) log.warn( "Saved game will be overwritten. Could not rename existing file: "+ chosenFile.getName() );
							}

							if ( bakValid ) {
								log.info( "Saved game was backed up: "+ bakName );
							}
						}

						out = new FileOutputStream( chosenFile );

						SavedGameParser parser = new SavedGameParser();
						FTLFrame.this.updateGameState(gameState);
						parser.writeSavedGame(out, gameState);
					}
					catch( IOException f ) {
						log.error( String.format("Error saving game state (\"%s\").", chosenFile.getName()), f );
						showErrorDialog( String.format("Error saving game state (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage()) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
				else {
					log.trace( "Save dialog cancelled." );
				}
			}
		});
		gameStateSaveBtn.addMouseListener( new StatusbarMouseListener(this, "Save the current game state.") );
		toolbar.add( gameStateSaveBtn );

		JButton gameStateDumpBtn = new JButton( "Dump", saveIcon );
		gameStateDumpBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "Dump game state button clicked." );

				if ( gameState == null ) return;

				JFileChooser dumpChooser = new JFileChooser();
				dumpChooser.setCurrentDirectory( fc.getCurrentDirectory() );
				dumpChooser.setFileHidingEnabled( false );

				ExtensionFileFilter txtFilter = new ExtensionFileFilter( "Text Files (*.txt)", new String[] {".txt"} );
				dumpChooser.addChoosableFileFilter( txtFilter );

				int chooserResponse = dumpChooser.showSaveDialog(FTLFrame.this);
				File chosenFile = dumpChooser.getSelectedFile();
				boolean sillyMistake = false;

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( !chosenFile.exists() && dumpChooser.getFileFilter() == txtFilter && !txtFilter.accept(chosenFile) ) {
						chosenFile = new File( chosenFile.getAbsolutePath() + txtFilter.getPrimarySuffix() );
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION ) {
					if ( "ae_prof.sav".equals(chosenFile.getName()) ||
					     "prof.sav".equals(chosenFile.getName()) ||
					     "continue.sav".equals(chosenFile.getName()) ) {

						int sillyResponse = JOptionPane.showConfirmDialog( FTLFrame.this, "Warning: What you are attempting makes no sense.\n\nYou're dumping a text summary called \""+ chosenFile.getName() +"\".\n\nAre you sure you know what you're doing?", "Really!?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
						if ( sillyResponse != JOptionPane.YES_OPTION ) sillyMistake = true;
					}
				}

				if ( chooserResponse == JFileChooser.APPROVE_OPTION && !sillyMistake ) {
					BufferedWriter out = null;
					try {
						log.info( "Dumping game state: "+ chosenFile.getAbsolutePath() );

						out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( chosenFile ) ) );
						out.write( gameState.toString() );
						out.close();
					}
					catch( IOException f ) {
						log.error( String.format("Error dumping game state (\"%s\").", chosenFile.getName()), f );
						showErrorDialog( String.format("Error dumping game state (\"%s\"):\n%s: %s", chosenFile.getName(), f.getClass().getSimpleName(), f.getMessage()) );
					}
					finally {
						try {if ( out != null ) out.close();}
						catch ( IOException f ) {}
					}
				}
				else {
					log.trace( "Dump dialog cancelled." );
				}
			}
		});
		gameStateDumpBtn.addMouseListener( new StatusbarMouseListener(this, "Dump unmodified game state info to a text file.") );
		toolbar.add( gameStateDumpBtn );

		toolbar.add( Box.createHorizontalGlue() );

		JButton gameStateAboutBtn = createAboutButton();
		toolbar.add( gameStateAboutBtn );

		JButton gameStateUpdatesBtn = createUpdatesButton();
		updatesButtonList.add( gameStateUpdatesBtn );
		toolbar.add( gameStateUpdatesBtn );
	}

	public JButton createAboutButton() {
		JButton aboutButton = new JButton( "About", aboutIcon );
		aboutButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				log.trace( "About button clicked." );

				JDialog aboutDialog = createAboutDialog();
				aboutDialog.setVisible( true );
			}
		});
		aboutButton.addMouseListener( new StatusbarMouseListener(this, "View information about this tool and links for information/bug reports") );
		return aboutButton;
	}

	public JButton createUpdatesButton() {
		JButton updatesButton = new JButton( "Updates" );
		updatesButton.setEnabled(false);
		updatesButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				if ( updatesCallback != null )
					updatesCallback.run();
			}
		});
		updatesButton.addMouseListener( new StatusbarMouseListener(this, "Update this tool or review past changes.") );
		return updatesButton;
	}

	private void checkForUpdate() {
		URL url = null;
		BufferedReader in = null;
		String line = null;
		try {
			log.trace( "Checking for latest version." );

			url = new URL( latestVersionUrl );
			in = new BufferedReader( new InputStreamReader( (InputStream)url.getContent(), Charset.forName("UTF-8").newDecoder() ) );
			int latestVersion = Integer.parseInt( in.readLine() );
			in.close();

			if ( latestVersion > appVersion ) {
				log.trace( "New version available." );

				final String historyHtml = getVersionHistoryHtml( latestVersionTemplate, appVersion );

				final Runnable newCallback = new Runnable() {
					@Override
					public void run() {
						log.trace( "Updates button clicked (new version)." );
						JDialog updatesDialog = createHtmlDialog( "Update Available", historyHtml );
						updatesDialog.setVisible( true );
					}
				};
				// Make changes from the GUI thread.
				Runnable r = new Runnable() {
					@Override
					public void run() {
						updatesCallback = newCallback;
						for ( JButton updatesButton : updatesButtonList ) {
							updatesButton.setBackground( new Color( 0xff, 0xaa, 0xaa ) );
							updatesButton.setIcon(updateIcon);
							updatesButton.setEnabled(true);
						}
						setStatusText( "A new version has been released." );
					}
				};
				SwingUtilities.invokeLater(r);
			}
			else {
				log.trace( "Already up-to-date." );

				final String historyHtml = getVersionHistoryHtml( releaseNotesTemplate, 0 );

				// Replacement behavior for the updates button.
				final Runnable newCallback = new Runnable() {
					@Override
					public void run() {
						log.trace( "Updates button clicked (release notes)." );
						JDialog updatesDialog = createHtmlDialog( "Release Notes", historyHtml );
						updatesDialog.setVisible( true );
					}
				};
				// Make changes from the GUI thread.
				Runnable r = new Runnable() {
					@Override
					public void run() {
						updatesCallback = newCallback;
						Color defaultColor = UIManager.getColor( "Button.background" );
						for ( JButton updatesButton : updatesButtonList ) {
							if ( defaultColor != null )
								updatesButton.setBackground( defaultColor );
							updatesButton.setIcon( releaseNotesIcon );
							updatesButton.setEnabled( true );
						}
						setStatusText( "No new updates." );
					}
				};
				SwingUtilities.invokeLater(r);
			}
		}
		catch ( java.net.UnknownHostException e ) {
			log.error( "Error checking for latest version. Unknown Host: "+ e.getMessage() );
			showErrorDialog( "Error checking for latest version.\n(Use the About window to check the download page manually)\nUnknown Host: "+ e.getMessage() );
		}
		catch ( Exception e ) {
			log.error( "Error checking for latest version.", e );
			showErrorDialog( "Error checking for latest version.\n(Use the About window to check the download page manually)\n"+ e );
		}
		finally {
			try {if ( in != null ) in.close();}
			catch ( IOException e ) {}
		}
	}

	private String getVersionHistoryHtml( URL templateUrl, int sinceVersion ) throws IOException {

		// Buffer for presentation-ready html.
		StringBuilder historyBuf = new StringBuilder();

		URL url = null;
		BufferedReader in = null;
		String line = null;
		try {
			// Fetch the template.
			StringBuilder templateBuf = new StringBuilder();
			url = templateUrl;
			in = new BufferedReader( new InputStreamReader( (InputStream)url.getContent() ) );
			while ( (line = in.readLine()) != null ) {
				templateBuf.append(line).append( "\n" );
			}
			in.close();
			String historyTemplate = templateBuf.toString();

			// Fetch the changelog, templating each revision.
			url = new URL( versionHistoryUrl );
			in = new BufferedReader( new InputStreamReader( (InputStream)url.getContent(), Charset.forName("UTF-8").newDecoder() ) );

			int releaseVersion = 0;
			StringBuilder releaseBuf = new StringBuilder();
			String releaseDesc = null;
			while ( (line = in.readLine()) != null ) {
				releaseVersion = Integer.parseInt( line );
				if ( releaseVersion <= sinceVersion ) break;

				releaseBuf.setLength(0);
				while ( (line = in.readLine()) != null && !line.equals("") ) {
					releaseBuf.append("<li>").append(line).append("</li>\n");
				}
				// Must've either hit a blank or done.

				if (releaseBuf.length() > 0) {
					String[] placeholders = new String[] { "{version}", "{items}" };
					String[] values = new String[] { "v"+releaseVersion, releaseBuf.toString() };
					releaseDesc = historyTemplate;
					for (int i=0; i < placeholders.length; i++)
						releaseDesc = releaseDesc.replaceAll(Pattern.quote(placeholders[i]), Matcher.quoteReplacement(values[i]) );
					historyBuf.append(releaseDesc);
				}
			}
			in.close();
		}
		finally {
			try {if ( in != null ) in.close();}
			catch ( IOException e ) {}
		}

		return historyBuf.toString();
	}

	private JDialog createHtmlDialog( String title, String content ) {

		JDialog dlg = new JDialog( this, title, true );
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );

		JEditorPane editor = new JEditorPane( "text/html", content );
		editor.setEditable( false );
		editor.setCaretPosition( 0 );
		editor.addHyperlinkListener( linkListener );
		editor.setTransferHandler( new HTMLEditorTransferHandler() );
		panel.add( new JScrollPane( editor ) );

		dlg.setContentPane( panel );
		dlg.setSize( 600, 400 );
		dlg.setLocationRelativeTo( this );

		return dlg;
	}

	private JDialog createBugReportDialog( String title, String message, String report ) {

		JDialog dlg = new JDialog( this, title, true );
		JPanel panel = new JPanel( new BorderLayout() );

		Font reportFont = new Font( "Monospaced", Font.PLAIN, 13 );

		JEditorPane messageEditor = new JEditorPane( "text/html", message );
		messageEditor.setEditable( false );
		messageEditor.setCaretPosition( 0 );
		messageEditor.addHyperlinkListener( linkListener );
		messageEditor.setTransferHandler( new HTMLEditorTransferHandler() );
		panel.add( new JScrollPane( messageEditor ), BorderLayout.NORTH );

		JTextArea reportArea = new JTextArea( report );
		reportArea.setTabSize( 4 );
		reportArea.setFont( reportFont );
		reportArea.setEditable( false );
		reportArea.setCaretPosition( 0 );
		panel.add( new JScrollPane( reportArea ), BorderLayout.CENTER );

		dlg.setContentPane( panel );
		dlg.setSize( 600, 450 );
		dlg.setLocationRelativeTo( this );

		return dlg;
	}

	private JDialog createAboutDialog() {

		JDialog dlg = new JDialog( this, "About", true );
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );

		try {
			JEditorPane editor = new JEditorPane( aboutPage );
			editor.setEditable( false );
			editor.addHyperlinkListener( linkListener );
			panel.add( editor );
		}
		catch ( IOException e ) {
			log.error(e);
		}

		dlg.setContentPane( panel );
		dlg.setSize( 300, 320 );
		dlg.setLocationRelativeTo( this );

		return dlg;
	}

	public void loadProfile( Profile p ) {
		try {
			log.trace( "Loading profile." );

			profileShipUnlockPanel.setProfile( p );
			profileGeneralAchsPanel.setProfile( p );
			profileGeneralStatsPanel.setProfile( p );
			profileShipStatsPanel.setProfile( p );
			profileDumpPanel.setText( (p != null ? p.toString() : "") );

			profile = p;
		}
		catch ( IOException e ) {
			log.error( "Error while loading profile.", e );

			if ( profile != null && profile != p ) {
				log.info( "Attempting to revert GUI to the previous profile..." );
				showErrorDialog( "Error loading profile.\nAttempting to return to the previous profile..." );
				loadProfile( profile );
			} else {
				showErrorDialog( "Error loading profile.\nThis has left the GUI in an ambiguous state.\nSaving is not recommended until another profile has successfully loaded." );
			}
		}

		this.repaint();
	}

	public void updateProfile( Profile p ) {
		log.trace( "Updating profile from UI selections." );

		profileShipUnlockPanel.updateProfile( p );
		profileGeneralAchsPanel.updateProfile( p );
		profileGeneralStatsPanel.updateProfile( p );
		profileShipStatsPanel.updateProfile( p );
		// profileDumpPanel doesn't modify anything.

		loadProfile(p);
	}

	/**
	 * Returns the currently loaded game state.
	 *
	 * This method should only be called when a panel
	 * needs to pull the state, make a major change,
	 * and reload it.
	 */
	public SavedGameParser.SavedGameState getGameState() {
		return gameState;
	}

	public void loadGameState( SavedGameParser.SavedGameState gs ) {
		savedGameDumpPanel.setText( (gs != null ? gs.toString() : "") );

		if ( gs != null && gs.getHeaderAlpha() == 2 ) {
			savedGameGeneralPanel.setGameState( gs );
			savedGamePlayerFloorplanPanel.setShipState( gs, gs.getPlayerShipState() );
			savedGameNearbyFloorplanPanel.setShipState( gs, gs.getNearbyShipState() );
			savedGameHangarPanel.setGameState( gs );
			savedGameSectorMapPanel.setGameState( gs );
			savedGameSectorTreePanel.setGameState( gs );
			savedGameStateVarsPanel.setGameState( gs );

			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_GENERAL ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_PLAYER_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_NEARBY_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_CHANGE_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_MAP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_TREE ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_STATE_VARS ), true );
			gameStateSaveBtn.setEnabled( true );
		}
		else if ( gs != null && ( gs.getHeaderAlpha() == 7 || gs.getHeaderAlpha() == 8 || gs.getHeaderAlpha() == 9 ) ) {
			// FTL 1.5.4 is only partially editable.

			savedGameGeneralPanel.setGameState( gs );
			savedGamePlayerFloorplanPanel.setShipState( gs, gs.getPlayerShipState() );
			savedGameNearbyFloorplanPanel.setShipState( gs, gs.getNearbyShipState() );
			savedGameHangarPanel.setGameState( gs );
			savedGameSectorMapPanel.setGameState( gs );
			savedGameSectorTreePanel.setGameState( gs );
			savedGameStateVarsPanel.setGameState( gs );

			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_GENERAL ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_PLAYER_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_NEARBY_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_CHANGE_SHIP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_MAP ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_TREE ), true );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_STATE_VARS ), true );
			savedGameTabsPane.setSelectedIndex( savedGameTabsPane.indexOfTab( SAVE_DUMP ) );
			gameStateSaveBtn.setEnabled( true );
		}
		else {
			savedGameGeneralPanel.setGameState( null );
			savedGamePlayerFloorplanPanel.setShipState( null, null );
			savedGameNearbyFloorplanPanel.setShipState( null, null );
			savedGameHangarPanel.setGameState( null );
			savedGameSectorMapPanel.setGameState( null );
			savedGameSectorTreePanel.setGameState( gs );
			savedGameStateVarsPanel.setGameState( null );

			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_GENERAL ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_PLAYER_SHIP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_NEARBY_SHIP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_CHANGE_SHIP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_MAP ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_SECTOR_TREE ), false );
			savedGameTabsPane.setEnabledAt( savedGameTabsPane.indexOfTab( SAVE_STATE_VARS ), false );
			savedGameTabsPane.setSelectedIndex( savedGameTabsPane.indexOfTab( SAVE_DUMP ) );
			gameStateSaveBtn.setEnabled( false );
		}

		gameState = gs;
	}

	public void updateGameState( SavedGameParser.SavedGameState gs ) {
		if ( gs != null && gs.getHeaderAlpha() == 2 ) {
			// savedGameDumpPanel doesn't modify anything.
			savedGameGeneralPanel.updateGameState( gs );
			savedGamePlayerFloorplanPanel.updateShipState( gs.getPlayerShipState() );
			savedGameNearbyFloorplanPanel.updateShipState( gs.getNearbyShipState() );
			// savedGameHangarPanel doesn't modify anything.
			savedGameSectorMapPanel.updateGameState( gs );
			savedGameSectorTreePanel.updateGameState( gs );
			savedGameStateVarsPanel.updateGameState( gs );

			// Sync session's redundant ship info with player ship.
			gs.setPlayerShipName( gs.getPlayerShipState().getShipName() );
			gs.setPlayerShipBlueprintId( gs.getPlayerShipState().getShipBlueprintId() );
		}
		else if ( gs != null && ( gs.getHeaderAlpha() == 7 | gs.getHeaderAlpha() == 8 || gs.getHeaderAlpha() == 9 ) ) {
			// FTL 1.5.4 is only partially editable.

			// savedGameDumpPanel doesn't modify anything.
			savedGameGeneralPanel.updateGameState( gs );
			savedGamePlayerFloorplanPanel.updateShipState( gs.getPlayerShipState() );
			savedGameNearbyFloorplanPanel.updateShipState( gs.getNearbyShipState() );
			// savedGameHangarPanel doesn't modify anything.
			savedGameSectorMapPanel.updateGameState( gs );
			savedGameSectorTreePanel.updateGameState( gs );
			savedGameStateVarsPanel.updateGameState( gs );

			// Sync session's redundant ship info with player ship.
			gs.setPlayerShipName( gs.getPlayerShipState().getShipName() );
			gs.setPlayerShipBlueprintId( gs.getPlayerShipState().getShipBlueprintId() );
		}

		loadGameState(gs);
	}

	public void setStatusText( String text ) {
		if (text.length() > 0) {
			statusLbl.setText( text );
		} else {
			statusLbl.setText( " " );
		}
	}
}

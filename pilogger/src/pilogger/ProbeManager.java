package pilogger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Timer;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import probes.AbstractProbe;

public class ProbeManager implements ActionListener {
	public static final String onlineFileLocalDirectory = "/home/pi/projects/pilogger/logs/online/";
	private static final int MS_TO_UPLOAD = 600000;  // 10min
	private PiloggerGUI gui;
	private HashMap<JMenuItem, DataChannel> scale0channelMap = new HashMap<>();
	private HashMap<JMenuItem, DataChannel> scale1channelMap = new HashMap<>();
	private HashMap<TimeScale, String> timeScaleText = new HashMap<>();
	private DataChannel scale0selectedChannel;
	private DataChannel scale1selectedChannel;
	private TimeScale timeScaleSelected = TimeScale.MONTH;

	/**
	 * Manage the probes by generating Gui according to 
	 * the channels provided. Switch accordingly the dataset
	 * displayed in chart.
	 * @param gui Pilogger Gui.
	 */
	public ProbeManager(PiloggerGUI gui) {
		this.gui = gui;
		initTimeScaleMenu();
		
		Timer t = new Timer();
		t.schedule(new UploadFTP(), MS_TO_UPLOAD/5, MS_TO_UPLOAD);
	}
	
	public void addProbe(final AbstractProbe probe) {
		for (int i = 0; i < probe.getChannels().length; i++) {
			final DataChannel channel = probe.getChannels()[i]; 
			final JMenuItem item0 = new JMenuItem(channel.channelName);
			final JMenuItem item1 = new JMenuItem(channel.channelName);
			scale0channelMap.put(item0, channel);
			scale1channelMap.put(item1, channel);
			item0.setBackground(Color.black);
			item1.setBackground(Color.black);
			item0.setForeground(Color.white);
			item1.setForeground(Color.white);			
			item0.setPreferredSize(new Dimension(150, 10));
			item1.setPreferredSize(new Dimension(150, 10));
			item0.setFont(PiloggerGUI.labelFont);
			item1.setFont(PiloggerGUI.labelFont);
			item0.addActionListener(this);
			item1.addActionListener(this);


			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					gui.getScale0menu().add(item0);
					gui.getScale1menu().add(item1);			
					gui.getLedPanel().add(channel.getChannelButton());
					gui.getLedPanel().revalidate();
					gui.getChannelReloadPanel().add(channel.getReloadButton());
				}
			});

			// first default selection
			if (scale0selectedChannel == null)
				scale0selectedChannel = channel;
			else if (scale1selectedChannel == null) {
				scale1selectedChannel = channel;
				resetDisplayedDataset();
			}
		}

		if (probe.getGuiComponents() != null && probe.getGuiComponents().length > 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < probe.getGuiComponents().length; i++) {
						gui.getProbeCustomPanel().add(probe.getGuiComponents()[i]);
					}
				}
			});
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JMenuItem item = (JMenuItem)arg0.getSource();
		if (scale0channelMap.containsKey(item)) {
			scale0selectedChannel = scale0channelMap.get(item);
		}
		if (scale1channelMap.containsKey(item)) {
			scale1selectedChannel = scale1channelMap.get(item);
		}
		resetDisplayedDataset();
	}

	private void resetDisplayedDataset() {
		if (gui.getLineDataSource0().getDataSetsCount() >0)
			gui.getLineDataSource0().removeDataSet(0);

		if (gui.getLineDataSource1().getDataSetsCount() >0)
			gui.getLineDataSource1().removeDataSet(0);

		if (gui.getAreaDataSource0().getDataSetsCount() >1) {
			gui.getAreaDataSource0().removeDataSet(1);
			gui.getAreaDataSource0().removeDataSet(0);
		}
		if (gui.getAreaDataSource1().getDataSetsCount() >1) {
			gui.getAreaDataSource1().removeDataSet(1);
			gui.getAreaDataSource1().removeDataSet(0);
		}

		switch (timeScaleSelected) {
		case REALTIME:
			gui.getLineDataSource0().addDataSet(0, scale0selectedChannel.realTimeDataSet);
			gui.getLineDataSource1().addDataSet(0, scale1selectedChannel.realTimeDataSet);
			break;

		case HOUR:
			gui.getAreaDataSource1().addDataSet(0, scale1selectedChannel.hourMaxDataSet);
			gui.getAreaDataSource1().addDataSet(1, scale1selectedChannel.hourMinDataSet);
			gui.getAreaDataSource0().addDataSet(0, scale0selectedChannel.hourMaxDataSet);
			gui.getAreaDataSource0().addDataSet(1, scale0selectedChannel.hourMinDataSet);
			gui.getLineDataSource1().addDataSet(0, scale1selectedChannel.hourDataSet);	
			gui.getLineDataSource0().addDataSet(0, scale0selectedChannel.hourDataSet);			
			break;

		case DAY:
			gui.getAreaDataSource1().addDataSet(0, scale1selectedChannel.dayMaxDataSet);
			gui.getAreaDataSource1().addDataSet(1, scale1selectedChannel.dayMinDataSet);
			gui.getAreaDataSource0().addDataSet(0, scale0selectedChannel.dayMaxDataSet);
			gui.getAreaDataSource0().addDataSet(1, scale0selectedChannel.dayMinDataSet);
			gui.getLineDataSource1().addDataSet(0, scale1selectedChannel.dayDataSet);	
			gui.getLineDataSource0().addDataSet(0, scale0selectedChannel.dayDataSet);			
			break;

		case MONTH:
			gui.getAreaDataSource1().addDataSet(0, scale1selectedChannel.monthMaxDataSet);
			gui.getAreaDataSource1().addDataSet(1, scale1selectedChannel.monthMinDataSet);
			gui.getAreaDataSource0().addDataSet(0, scale0selectedChannel.monthMaxDataSet);
			gui.getAreaDataSource0().addDataSet(1, scale0selectedChannel.monthMinDataSet);
			gui.getLineDataSource1().addDataSet(0, scale1selectedChannel.monthDataSet);			
			gui.getLineDataSource0().addDataSet(0, scale0selectedChannel.monthDataSet);			
			break;

		case YEAR:
			gui.getAreaDataSource1().addDataSet(0, scale1selectedChannel.yearMaxDataSet);
			gui.getAreaDataSource1().addDataSet(1, scale1selectedChannel.yearMinDataSet);
			gui.getAreaDataSource0().addDataSet(0, scale0selectedChannel.yearMaxDataSet);
			gui.getAreaDataSource0().addDataSet(1, scale0selectedChannel.yearMinDataSet);
			gui.getLineDataSource1().addDataSet(0, scale1selectedChannel.yearDataSet);			
			gui.getLineDataSource0().addDataSet(0, scale0selectedChannel.yearDataSet);			
			break;

		default:
			gui.getLineDataSource0().addDataSet(0, scale0selectedChannel.realTimeDataSet);			
			gui.getLineDataSource1().addDataSet(0, scale1selectedChannel.realTimeDataSet);
			break;
		}

		gui.getScale0Button().setText(scale0selectedChannel.channelName);
		gui.getScale1Button().setText(scale1selectedChannel.channelName);
		gui.getTimeScaleButton().setText(timeScaleText.get(timeScaleSelected));
	}	
	private void initTimeScaleMenu() {
		timeScaleText.put(TimeScale.REALTIME, "Real Time");
		timeScaleText.put(TimeScale.HOUR, "Hour");
		timeScaleText.put(TimeScale.DAY, "Day");
		timeScaleText.put(TimeScale.MONTH, "Month");
		timeScaleText.put(TimeScale.YEAR, "Year");
		gui.getScaleTimeMenu().add(getRealTimeMItem());
		gui.getScaleTimeMenu().add(getHourTimeMItem());
		gui.getScaleTimeMenu().add(getDayTimeMItem());
		gui.getScaleTimeMenu().add(getMonthTimeMItem());
		gui.getScaleTimeMenu().add(getYearTimeMItem());
	} 

	private JMenuItem realTimeMItem;
	private JMenuItem getRealTimeMItem() {
		if (realTimeMItem == null) {
			realTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.REALTIME));
			realTimeMItem.setBackground(Color.black);
			realTimeMItem.setForeground(Color.white);
			realTimeMItem.setFont(PiloggerGUI.labelFont);
			realTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.REALTIME;
					resetDisplayedDataset();
				}
			});
		}
		return realTimeMItem;
	}
	private JMenuItem hourTimeMItem;
	private JMenuItem getHourTimeMItem() {
		if (hourTimeMItem == null) {
			hourTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.HOUR));
			hourTimeMItem.setBackground(Color.black);
			hourTimeMItem.setForeground(Color.white);
			hourTimeMItem.setFont(PiloggerGUI.labelFont);
			hourTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.HOUR;
					resetDisplayedDataset();
				}
			});
		}
		return hourTimeMItem;
	}
	private JMenuItem dayTimeMItem;
	private JMenuItem getDayTimeMItem() {
		if (dayTimeMItem == null) {
			dayTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.DAY));
			dayTimeMItem.setBackground(Color.black);
			dayTimeMItem.setForeground(Color.white);
			dayTimeMItem.setFont(PiloggerGUI.labelFont);
			dayTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.DAY;
					resetDisplayedDataset();
				}
			});
		}
		return dayTimeMItem;
	}
	private JMenuItem monthTimeMItem;
	private JMenuItem getMonthTimeMItem() {
		if (monthTimeMItem == null) {
			monthTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.MONTH));
			monthTimeMItem.setBackground(Color.black);
			monthTimeMItem.setForeground(Color.white);
			monthTimeMItem.setFont(PiloggerGUI.labelFont);
			monthTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.MONTH;
					resetDisplayedDataset();
				}
			});
		}
		return monthTimeMItem;
	}
	private JMenuItem yearTimeMItem;
	private JMenuItem getYearTimeMItem() {
		if (yearTimeMItem == null) {
			yearTimeMItem = new JMenuItem(timeScaleText.get(TimeScale.YEAR));
			yearTimeMItem.setBackground(Color.black);
			yearTimeMItem.setForeground(Color.white);
			yearTimeMItem.setFont(PiloggerGUI.labelFont);
			yearTimeMItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timeScaleSelected = TimeScale.YEAR;
					resetDisplayedDataset();
				}
			});
		}
		return yearTimeMItem;
	}
	
}

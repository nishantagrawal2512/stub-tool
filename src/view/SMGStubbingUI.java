package view;

import java.awt.BorderLayout;
import java.awt.Color;import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/************************************************************
 * 
 * Author: Shah Murad Hussain
 * Date: 18.03.2016
 * Version Number: 0.5
 * Accenture
 * 
 ************************************************************/


public class SMGStubbingUI{
	
	private JFrame mainFrame;
	private JPanel headerPanel,bodyPanel,footerPanel;
	private JLabel headerLabel;
	private JLabel statusLabel;
	private Font standardFont;
	private Color standardFontColour;
	
	public SMGStubbingUI(){
		initUI();
		
	}
	
	private void initUI(){
		mainFrame = new JFrame("SMG Stub Server - SHAH");
		standardFont = new Font("SANS_SERIF", Font.BOLD, 14);
		standardFontColour = new Color(255, 255, 255);
		mainFrame.setSize(600, 600);
		mainFrame.setLayout(new BorderLayout());
		
		
		mainFrame.add(createHeaderPanel(),BorderLayout.NORTH);
		mainFrame.add(createBodyPanel(),BorderLayout.CENTER);
		mainFrame.add(createFooterPanel(),BorderLayout.SOUTH);
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
	}
	
	private JPanel createHeaderPanel(){
		
		headerPanel = new JPanel(new FlowLayout());
		headerLabel = new JLabel("SMG Stub Server",JLabel.CENTER);
		headerLabel.setFont(new Font("SANS_SERIF", Font.BOLD, 36));
		headerLabel.setForeground(new Color(255, 255, 255));
		headerPanel.add(headerLabel);
		headerPanel.setBackground(new Color(59,89,152));
		
		return headerPanel;
	}
	
	private JPanel createBodyPanel(){
		
		/*
		 * # User Configuration
		portNumber=8072
		
		#mdm details
		mdmURL=http://plylmdmappt01:8096/DCCWSInboundCGIAdapter/ResponseAndAlert
		mdmUsername=EDFAdmin
		mdmPassword=3DF@dmin
		
		#Declared namespace 
		#for example if an xml tag is structured as <ser:Header> then the declared namespace will be ser:
		declared_namespace=ser:
		 * 
		 * */
		
		bodyPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		JLabel portNumber, xmlNamespace,mdmURL, mdmUsername, mdmPassword;
		JTextField portNumberTextField,mdmURLField,xmlNameSpaceField, mdmUsernameField,mdmPasswordField;
		JButton startServer;
		

		portNumber = new JLabel("Port Number:",JLabel.CENTER);
		portNumber.setForeground(standardFontColour);
		portNumber.setFont(standardFont);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 0;
		constraints.gridx = 1;
		constraints.insets = new Insets(4,10,4,10);
		bodyPanel.add(portNumber, constraints);
		

		portNumberTextField = new JTextField("8080");
		portNumberTextField.setBorder(new EmptyBorder(2,10,2,15));
		//constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 0;
		constraints.gridx = 2;
		
		
		bodyPanel.add(portNumberTextField, constraints);
		
		xmlNamespace = new JLabel("Declared XML Namespace:",JLabel.CENTER);
		xmlNamespace.setForeground(standardFontColour);
		xmlNamespace.setFont(standardFont);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 1;
		constraints.gridx = 1;
		bodyPanel.add(xmlNamespace, constraints);
		
		xmlNameSpaceField = new JTextField();
		xmlNameSpaceField.setBorder(new EmptyBorder(2,10,2,15));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 1;
		constraints.gridx = 2;
		bodyPanel.add(xmlNameSpaceField,constraints);
		
		
		mdmURL = new JLabel("MDM URL:",JLabel.CENTER);
		mdmURL.setForeground(standardFontColour);
		mdmURL.setFont(standardFont);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 2;
		constraints.gridx = 1;
		bodyPanel.add(mdmURL, constraints);
		
		mdmURLField = new JTextField();
		mdmURLField.setBorder(new EmptyBorder(2,10,2,15));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 2;
		constraints.gridx = 2;
		bodyPanel.add(mdmURLField,constraints);
		
		mdmUsername = new JLabel("MDM Username:",JLabel.CENTER);
		mdmUsername.setForeground(standardFontColour);
		mdmUsername.setFont(standardFont);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 3;
		constraints.gridx = 1;
		bodyPanel.add(mdmUsername, constraints);
		
		mdmUsernameField = new JTextField();
		mdmUsernameField.setBorder(new EmptyBorder(2,10,2,15));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 3;
		constraints.gridx = 2;
		bodyPanel.add(mdmUsernameField,constraints);
		
		mdmPassword = new JLabel("MDM Password:",JLabel.CENTER);
		mdmPassword.setForeground(standardFontColour);
		mdmPassword.setFont(standardFont);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 4;
		constraints.gridx = 1;
		bodyPanel.add(mdmPassword, constraints);
		
		mdmPasswordField = new JTextField();
		mdmPasswordField.setBorder(new EmptyBorder(2,10,2,15));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.3;
		constraints.gridy = 4;
		constraints.gridx = 2;
		bodyPanel.add(mdmPasswordField,constraints);
		
		startServer = new JButton ("Start Server");
		startServer.setFont(standardFont);
		startServer.setBackground(new Color(223,227,238));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.0;
		//constraints.gridwidth =2;
		constraints.gridy = 5;
		constraints.gridx = 2;
		
		bodyPanel.setBackground(new Color(59,89,152));
		bodyPanel.add(startServer,constraints);
		bodyPanel.setPreferredSize(new Dimension(550, 200));
		
		return bodyPanel;
		
	}
	
	private JPanel createFooterPanel(){
		
		footerPanel = new JPanel(new FlowLayout());
		JTextArea console = new JTextArea();
		console.setEditable(true);
		
		//console.setBorder(new EmptyBorder(50,275,50,275));
		//console.setMaximumSize(new Dimension(400, 275));
		
		JScrollPane scrollPane = new JScrollPane(console);
		//scrollPane.setPreferredSize(new Dimension(550, 200));
		scrollPane.setPreferredSize(new Dimension(550,200 ));
		
		//headerLabel = new JLabel("SMG Stubbing",JLabel.CENTER);
		//footerPanel.add(headerLabel);
		footerPanel.add(scrollPane);
		footerPanel.setBackground(new Color(224,224,224));
		
		
		return footerPanel;
	}
	
	
	
	public static void main(String[] args) {
		SMGStubbingUI ui = new SMGStubbingUI();
	}
}

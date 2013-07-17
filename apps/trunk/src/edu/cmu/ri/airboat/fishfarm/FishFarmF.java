/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FishFarmF.java
 *
 * Created on Jan 19, 2012, 9:12:58 PM
 */
package edu.cmu.ri.airboat.fishfarm;

import edu.cmu.ri.airboat.client.BoatDebugPanel;
import edu.cmu.ri.airboat.client.gui.TeleopFrame;
import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.airboat.general.ConfigureBoatsFrame;
import edu.cmu.ri.airboat.general.ProxyManager;
import edu.cmu.ri.airboat.general.ProxyManagerListener;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CrwSecurityManager;
import gov.nasa.worldwind.geom.LatLon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;

/**
 *
 * @author pscerri
 */
public class FishFarmF extends javax.swing.JFrame {

    private final String MIN_LAT_S = "MIN_LAT_S";
    private final String MAX_LAT_S = "MAX_LAT_S";
    private final String MIN_LON_S = "MIN_LON_S";
    private final String MAX_LON_S = "MAX_LON_S";
    private final String UPPER_BOUND_TF_S = "UPPER_BOUND_TF_S";
    private final String LOWER_BOUND_TF_S = "LOWER_BOUND_TF_S";
    private final String CONTOUR_VALUE_TF_S = "CONTOUR_VALUE_TF_S";
    private final String CONTOUR_TF_S = "CONTOUR_TF_S";
    
    private LatLon mins = LatLon.ZERO;
    private LatLon maxs = LatLon.ZERO;
    
    // Temp for experiments
    public static ArrayList<FishFarmBoatProxy> proxies = new ArrayList<FishFarmBoatProxy>();
    private DataManager dm = new DataManager(proxies);
    private DecimalFormat df = new DecimalFormat("#.###");
    // @todo make this cleaner, probably a listener on Repo
    public static DefaultComboBoxModel indexCDataModel = new DefaultComboBoxModel();
    edu.cmu.ri.airboat.client.gui.TeleopFrame teleOpFrame = null;
    
    ImagePanel imageP = new ImagePanel();
    
    Point dragStart = null;
    Point dragLast = null;

    /** Creates new form FishFarmF */
    public FishFarmF() {
        initComponents();

        imagePContainer.setLayout(new BorderLayout());
        imagePContainer.add(imageP, BorderLayout.CENTER);
        
        imgTypeCombo.setModel(new DefaultComboBoxModel(DataRepository.ImageType.values()));

        try {

            longMinTF.setText(Preferences.userRoot().get(MIN_LON_S, "40.0"));
            longMaxTF.setText(Preferences.userRoot().get(MAX_LON_S, "41.0"));
            latMinTF.setText(Preferences.userRoot().get(MIN_LAT_S, "80.0"));
            latMaxTF.setText(Preferences.userRoot().get(MAX_LAT_S, "81.0"));

            mins = LatLon.fromDegrees(Double.parseDouble(latMinTF.getText()), Double.parseDouble(longMinTF.getText()));
            maxs = LatLon.fromDegrees(Double.parseDouble(latMaxTF.getText()), Double.parseDouble(longMaxTF.getText()));

            upperBTF.setText(Preferences.userRoot().get(UPPER_BOUND_TF_S, "85.0"));
            lowerBTF.setText(Preferences.userRoot().get(LOWER_BOUND_TF_S, "75.0"));
            
            contourValueTF.setText(Preferences.userRoot().get(CONTOUR_VALUE_TF_S, "100.0"));
            contourTF.setText(Preferences.userRoot().get(CONTOUR_TF_S, "50.0"));
            
        } catch (AccessControlException e) {
            System.out.println("Failed to access preferences");
        }


        ProxyManagerListener listener = new ProxyManagerListener() {

            public void proxyAdded(BoatProxy bp) {
                FishFarmBoatProxy fb = new FishFarmBoatProxy(bp, dm);
                fb.setRepo(dm.repo);
                proxies.add(fb);

                ((DefaultComboBoxModel) proxyC.getModel()).addElement(fb);
            }
        };
        (new ProxyManager()).addListener(listener);

        dm.setExtent(mins, maxs);
        dataViewP.setLayout(new BorderLayout());
        dataViewP.add(dm, BorderLayout.CENTER);

        proxyC.setRenderer(new ProxyComboRenderer());
        proxyC.setEditable(false);

        algC.setSelectedItem(dm.repo.getAlg());

        contourValueTF.setText("" + dm.repo.getContourValue());

        indexC.setModel(indexCDataModel);
        
        dm.repo.setLatestTP(latestTP);
        
        dataViewP.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.isShiftDown() && proxyC.getSelectedItem() != null) {                    
                    gov.nasa.worldwind.geom.Position pos = dm.repo.getPositionFor((double)me.getPoint().x/dataViewP.getWidth(), 1.0 - ((double)me.getPoint().y/dataViewP.getHeight()));
                    System.out.println("HUMAN SET WAYPOINT: " + proxyC.getSelectedItem() + " " + pos);
                    ((FishFarmBoatProxy)proxyC.getSelectedItem()).setWaypoint(pos);
                }
                 
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                dragStart = null;
                dragLast = null;                                
            }                        
            
        });
        
        dataViewP.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent me) {
                super.mouseDragged(me);
                
                if (dragStart == null) {
                    System.out.println("Drag starting");
                    dragStart = me.getPoint();
                    dragLast = me.getPoint();
                } else {
                    Point curr = me.getPoint();
                    
                    
                    if (curr.y < dragLast.y) {                        
                        dm.repo.changeValues((double)dragStart.x/dataViewP.getWidth(), 1.0 - ((double)dragStart.y/dataViewP.getHeight()), true);                
                    } else if (curr.y > dragLast.y) {
                        dm.repo.changeValues((double)dragStart.x/dataViewP.getWidth(), 1.0 - ((double)dragStart.y/dataViewP.getHeight()), false);
                    }
                    
                    dragLast = curr;
                    
                    dataViewP.repaint();
                }
            }
            
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        latMaxTF = new javax.swing.JTextField();
        latMinTF = new javax.swing.JTextField();
        longMinTF = new javax.swing.JTextField();
        dataViewP = new javax.swing.JPanel();
        longMaxTF = new javax.swing.JTextField();
        imgTypeCombo = new javax.swing.JComboBox();
        proxyP = new javax.swing.JPanel();
        proxyC = new javax.swing.JComboBox();
        teleopB = new javax.swing.JButton();
        autoCB = new javax.swing.JCheckBox();
        algC = new javax.swing.JComboBox();
        contourP = new javax.swing.JPanel();
        contourTF = new javax.swing.JTextField();
        contourS = new javax.swing.JSlider();
        contourValueTF = new javax.swing.JTextField();
        indexC = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        upperBTF = new javax.swing.JTextField();
        lowerBTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        latestTP = new javax.swing.JTextPane();
        imagePContainer = new javax.swing.JPanel();
        swarmingCB = new javax.swing.JCheckBox();
        speedS = new javax.swing.JSlider();
        distS = new javax.swing.JSlider();
        allAutoCB = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        latMaxTF.setText("jTextField1");
        latMaxTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latMaxTFActionPerformed(evt);
            }
        });

        latMinTF.setText("jTextField2");
        latMinTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latMinTFActionPerformed(evt);
            }
        });

        longMinTF.setText("jTextField3");
        longMinTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longMinTFActionPerformed(evt);
            }
        });

        dataViewP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.jdesktop.layout.GroupLayout dataViewPLayout = new org.jdesktop.layout.GroupLayout(dataViewP);
        dataViewP.setLayout(dataViewPLayout);
        dataViewPLayout.setHorizontalGroup(
            dataViewPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 767, Short.MAX_VALUE)
        );
        dataViewPLayout.setVerticalGroup(
            dataViewPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        longMaxTF.setText("jTextField4");
        longMaxTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longMaxTFActionPerformed(evt);
            }
        });

        imgTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        imgTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgTypeComboActionPerformed(evt);
            }
        });

        proxyP.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        proxyC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxyCActionPerformed(evt);
            }
        });

        teleopB.setText("TeleOp");
        teleopB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teleopBActionPerformed(evt);
            }
        });

        autoCB.setText("Auto");
        autoCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout proxyPLayout = new org.jdesktop.layout.GroupLayout(proxyP);
        proxyP.setLayout(proxyPLayout);
        proxyPLayout.setHorizontalGroup(
            proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, proxyPLayout.createSequentialGroup()
                .addContainerGap()
                .add(proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(proxyC, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(proxyPLayout.createSequentialGroup()
                        .add(autoCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(teleopB)))
                .add(75, 75, 75))
        );
        proxyPLayout.setVerticalGroup(
            proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(proxyPLayout.createSequentialGroup()
                .addContainerGap()
                .add(proxyC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(proxyPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(teleopB)
                    .add(autoCB))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        algC.setModel(new DefaultComboBoxModel(DataRepository.AutonomyAlgorithm.values()));
        algC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algCActionPerformed(evt);
            }
        });

        contourP.setBorder(javax.swing.BorderFactory.createTitledBorder("Contour Precentile"));

        contourTF.setText("0.0");
        contourTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contourTFActionPerformed(evt);
            }
        });

        contourS.setPaintLabels(true);
        contourS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                contourSStateChanged(evt);
            }
        });

        contourValueTF.setText("0.0");
        contourValueTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contourValueTFActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout contourPLayout = new org.jdesktop.layout.GroupLayout(contourP);
        contourP.setLayout(contourPLayout);
        contourPLayout.setHorizontalGroup(
            contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contourPLayout.createSequentialGroup()
                .add(contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(contourPLayout.createSequentialGroup()
                        .add(contourTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(contourValueTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(contourS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contourPLayout.setVerticalGroup(
            contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contourPLayout.createSequentialGroup()
                .add(contourPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contourTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(contourValueTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(contourS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        indexC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0" }));
        indexC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexCActionPerformed(evt);
            }
        });

        jButton1.setText("Context");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        upperBTF.setText("100.0");
        upperBTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upperBTFActionPerformed(evt);
            }
        });

        lowerBTF.setText("0.0");
        lowerBTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowerBTFActionPerformed(evt);
            }
        });

        jLabel1.setText("Upper");

        jLabel2.setText("Lower");

        latestTP.setEditable(false);
        latestTP.setFont(new java.awt.Font("Optima", 0, 24)); // NOI18N
        latestTP.setForeground(new java.awt.Color(204, 0, 0));
        latestTP.setText("Latest data");
        jScrollPane1.setViewportView(latestTP);

        org.jdesktop.layout.GroupLayout imagePContainerLayout = new org.jdesktop.layout.GroupLayout(imagePContainer);
        imagePContainer.setLayout(imagePContainerLayout);
        imagePContainerLayout.setHorizontalGroup(
            imagePContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        imagePContainerLayout.setVerticalGroup(
            imagePContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 215, Short.MAX_VALUE)
        );

        swarmingCB.setText("Swarming");
        swarmingCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swarmingCBActionPerformed(evt);
            }
        });

        speedS.setToolTipText("Speed slider");
        speedS.setValue(1);
        speedS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speedSStateChanged(evt);
            }
        });

        distS.setMaximum(500);
        distS.setToolTipText("Swarming distance");
        distS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                distSStateChanged(evt);
            }
        });

        allAutoCB.setText("All auto");
        allAutoCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allAutoCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(latMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(longMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(longMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(latMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(78, 78, 78)
                                .add(dataViewP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(0, 0, Short.MAX_VALUE)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(upperBTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lowerBTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1)
                            .add(proxyP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, contourP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(imgTypeCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(indexC, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(algC, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(imagePContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(distS, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(speedS, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(jButton1)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(allAutoCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(swarmingCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 404, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(imgTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(algC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(indexC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(imagePContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(distS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(speedS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(swarmingCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(upperBTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(lowerBTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contourP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(allAutoCB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(proxyP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(latMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dataViewP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(latMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(longMinTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(longMaxTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void longMaxTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longMaxTFActionPerformed

        maxs = LatLon.fromDegrees(maxs.latitude.degrees, Double.parseDouble(longMaxTF.getText()));
        dm.setExtent(mins, maxs);
        savePref(MAX_LON_S, longMaxTF.getText());
    }//GEN-LAST:event_longMaxTFActionPerformed

    private void latMinTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latMinTFActionPerformed

        mins = LatLon.fromDegrees(Double.parseDouble(latMinTF.getText()), mins.longitude.degrees);
        dm.setExtent(mins, maxs);
        savePref(MIN_LAT_S, latMinTF.getText());
    }//GEN-LAST:event_latMinTFActionPerformed

    private void longMinTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longMinTFActionPerformed

        mins = LatLon.fromDegrees(mins.latitude.degrees, Double.parseDouble(longMinTF.getText()));
        dm.setExtent(mins, maxs);
        savePref(MIN_LON_S, longMinTF.getText());
    }//GEN-LAST:event_longMinTFActionPerformed

    private void latMaxTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latMaxTFActionPerformed

        maxs = LatLon.fromDegrees(Double.parseDouble(latMaxTF.getText()), maxs.longitude.degrees);
        dm.setExtent(mins, maxs);
        savePref(MAX_LAT_S, latMaxTF.getText());
    }//GEN-LAST:event_latMaxTFActionPerformed

    private void imgTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgTypeComboActionPerformed
        dm.setImgType((DataRepository.ImageType) imgTypeCombo.getSelectedItem());
    }//GEN-LAST:event_imgTypeComboActionPerformed

    private void teleopBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teleopBActionPerformed
        if ((teleOpFrame != null && teleOpFrame.isVisible()) || proxyC.getSelectedItem() == null) {
        } else {
            teleOpFrame = new TeleopFrame(((FishFarmBoatProxy) proxyC.getSelectedItem()).getProxy().getVehicleServer());            
            teleOpFrame.setVisible(true);
            // System.out.println("Created teleop frame");
        }
    }//GEN-LAST:event_teleopBActionPerformed

    private void autoCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoCBActionPerformed

        if (proxyC.getSelectedItem() == null) {
            return;
        } else {
            FishFarmBoatProxy proxy = (FishFarmBoatProxy) proxyC.getSelectedItem();
            proxy.setAutonomous(autoCB.isSelected());
        }

    }//GEN-LAST:event_autoCBActionPerformed

    private void algCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algCActionPerformed
        dm.repo.setAlg((DataRepository.AutonomyAlgorithm) algC.getSelectedItem());
    }//GEN-LAST:event_algCActionPerformed

    private void proxyCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyCActionPerformed
        FishFarmBoatProxy p = (FishFarmBoatProxy) proxyC.getSelectedItem();
        autoCB.setSelected(p.isIsAutonomous());
        proxyP.setBorder(new EtchedBorder(p.getColor(), p.getColor().brighter()));
    }//GEN-LAST:event_proxyCActionPerformed

    private void contourTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contourTFActionPerformed
        try {
            int i = Integer.parseInt(contourTF.getText());
            contourS.setValue(i);
            savePref(CONTOUR_TF_S, contourTF.getText());                    
        } catch (NumberFormatException e) {
        }
    }//GEN-LAST:event_contourTFActionPerformed

    private void indexCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexCActionPerformed
        dm.repo.setIndexOfInterest((Integer) indexC.getSelectedItem());
    }//GEN-LAST:event_indexCActionPerformed

    private void contourSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_contourSStateChanged
        contourTF.setText("" + contourS.getValue());
        double v = dm.repo.setContourPercentOfMax(contourS.getValue() / 100.0);
        contourValueTF.setText(df.format(v));
        savePref(CONTOUR_TF_S, contourTF.getText());
    }//GEN-LAST:event_contourSStateChanged

    private void contourValueTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contourValueTFActionPerformed
        try {
            dm.repo.setContourValue(Double.parseDouble(contourValueTF.getText()));
            savePref(CONTOUR_VALUE_TF_S, contourValueTF.getText());
        } catch (NumberFormatException e) {
        }
    }//GEN-LAST:event_contourValueTFActionPerformed
    JFrame debugFrame = null;
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (debugFrame != null && debugFrame.isVisible()) {
        } else {


            FishFarmBoatProxy ffp = (FishFarmBoatProxy) proxyC.getSelectedItem();
            if (ffp != null) {
                BoatDebugPanel boatPanel = new BoatDebugPanel();
                boatPanel.setServer(ffp.getVehicleServer());

                JFrame mainFrame = new JFrame();
                mainFrame.setTitle("Boat Debugging Panel");
                mainFrame.getContentPane().add(boatPanel);
                mainFrame.setLocation(100, 100);
                mainFrame.pack();
                mainFrame.setVisible(true);
                mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                debugFrame = mainFrame;
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void lowerBTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowerBTFActionPerformed
        try {
            double ret = dm.setLowerBound(Double.parseDouble(lowerBTF.getText()));
            lowerBTF.setText("" + ret);
            savePref(LOWER_BOUND_TF_S, lowerBTF.getText());
        } catch (NumberFormatException e) {
            System.out.println("Badly formatted lower bound");
        }
    }//GEN-LAST:event_lowerBTFActionPerformed

    private void upperBTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upperBTFActionPerformed
        try {
            double ret = dm.setUpperBound(Double.parseDouble(upperBTF.getText()));
            upperBTF.setText("" + ret);
            savePref(UPPER_BOUND_TF_S, upperBTF.getText());
        } catch (NumberFormatException e) {
            System.out.println("Badly formatted upper bound");
        }
    }//GEN-LAST:event_upperBTFActionPerformed

    private void allAutoCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allAutoCBActionPerformed
        for (FishFarmBoatProxy fishFarmBoatProxy : proxies) {
            fishFarmBoatProxy.setAutonomous(allAutoCB.isSelected());
        }
        
        autoCB.setSelected(allAutoCB.isSelected());
    }//GEN-LAST:event_allAutoCBActionPerformed

    private void swarmingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swarmingCBActionPerformed
        if (swarmingCB.isSelected()) {
            System.out.println("Starting swarming");
            FishFarmBoatProxy leader = null;
            for (FishFarmBoatProxy fishFarmBoatProxy : proxies) {
                if (leader == null) {
                    System.out.println("Got a proxy to be leader");
                    leader = fishFarmBoatProxy;
                } else {
                    System.out.println("Got a proxy to be follower");
                    fishFarmBoatProxy.setSwarmFollower(leader);
                }
            }
        } else {
            System.out.println("Stopping swarming");
        }
    }//GEN-LAST:event_swarmingCBActionPerformed

    private void distSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_distSStateChanged
        FishFarmBoatProxy.swarmDist = (double)distS.getValue() / 100000.0;
        System.out.println("Dist now : " + FishFarmBoatProxy.swarmDist);
    }//GEN-LAST:event_distSStateChanged

    private void speedSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSStateChanged
        FishFarmBoatProxy.swarmSpeed = (double)speedS.getValue();
        System.out.println("Speed now: " + FishFarmBoatProxy.swarmSpeed);
    }//GEN-LAST:event_speedSStateChanged

    private void savePref(String key, String value) {
        try {
            Preferences p = Preferences.userRoot();
            p.put(key, value);
        } catch (AccessControlException e) {
            System.out.println("Failed to save preferences");
        }
    }

    private class ProxyComboRenderer extends JLabel implements ListCellRenderer {

        public ProxyComboRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            // System.out.println("Called with index " + index + " object " + value);

            if (value == null) {
                return new JLabel("None");
            }

            FishFarmBoatProxy proxy = (FishFarmBoatProxy) value;
            JLabel label = new JLabel(proxy.toString());

            label.setForeground(proxy.getColor());

            return label;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                CrwSecurityManager.loadIfDNSIsSlow();

                // End Cut and paste from BoatDebugger

                ConfigureBoatsFrame config = new ConfigureBoatsFrame();
                config.setVisible(true);

                new FishFarmF().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JComboBox algC;
    public static javax.swing.JCheckBox allAutoCB;
    private javax.swing.JCheckBox autoCB;
    private javax.swing.JPanel contourP;
    private javax.swing.JSlider contourS;
    private javax.swing.JTextField contourTF;
    private javax.swing.JTextField contourValueTF;
    private javax.swing.JPanel dataViewP;
    private javax.swing.JSlider distS;
    private javax.swing.JPanel imagePContainer;
    private javax.swing.JComboBox imgTypeCombo;
    private javax.swing.JComboBox indexC;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField latMaxTF;
    private javax.swing.JTextField latMinTF;
    private javax.swing.JTextPane latestTP;
    private javax.swing.JTextField longMaxTF;
    private javax.swing.JTextField longMinTF;
    private javax.swing.JTextField lowerBTF;
    private javax.swing.JComboBox proxyC;
    private javax.swing.JPanel proxyP;
    private javax.swing.JSlider speedS;
    private javax.swing.JCheckBox swarmingCB;
    private javax.swing.JButton teleopB;
    private javax.swing.JTextField upperBTF;
    // End of variables declaration//GEN-END:variables
}

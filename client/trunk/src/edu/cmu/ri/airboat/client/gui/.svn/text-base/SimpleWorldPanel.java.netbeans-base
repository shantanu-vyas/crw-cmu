/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client.gui;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Earth.USGSDigitalOrtho;
import gov.nasa.worldwind.layers.Earth.USGSTopoHighRes;
import gov.nasa.worldwind.layers.Earth.USGSTopographicMaps;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

/**
 * Simple display class for putting a single boat on the map.
 *
 * @author pkv
 */
public class SimpleWorldPanel extends WorldWindowGLJPanel {

    public final Marker boat = new BasicMarker(Position.ZERO, new BasicMarkerAttributes());
    public final Marker waypoint = new BasicMarker(Position.ZERO, new BasicMarkerAttributes());
    public final Marker click = new BasicMarker(Position.ZERO, new BasicMarkerAttributes());

    public SimpleWorldPanel() {
        setPreferredSize(new Dimension(600, 600));
        setModel(new BasicModel());

        boat.getAttributes().setOpacity(0.0);
        boat.setHeading(Angle.ZERO);
        boat.getAttributes().setShapeType(BasicMarkerShape.ORIENTED_SPHERE);
        boat.getAttributes().setMaterial(Material.ORANGE);

        waypoint.getAttributes().setOpacity(0.0);
        waypoint.getAttributes().setShapeType(BasicMarkerShape.CYLINDER);
        waypoint.getAttributes().setMaterial(Material.MAGENTA);

        click.getAttributes().setOpacity(0.0);
        click.getAttributes().setShapeType(BasicMarkerShape.SPHERE);
        click.getAttributes().setMaterial(Material.BLUE);

        ArrayList markers = new ArrayList();
        markers.add(boat);
        markers.add(waypoint);
        markers.add(click);

        MarkerLayer markerLayer = new MarkerLayer();
        markerLayer.setOverrideMarkerElevation(true);
        markerLayer.setKeepSeparated(false);
        markerLayer.setElevation(10d);
        markerLayer.setMarkers(markers);
        markerLayer.setPickEnabled(true);
        getModel().getLayers().add(markerLayer);

        final USGSTopographicMaps usgsTopo = new USGSTopographicMaps();
        final USGSDigitalOrtho usgslayerOrtho = new USGSDigitalOrtho();
        final USGSTopoHighRes usgsTopoHR = new USGSTopoHighRes();
        getModel().getLayers().add(usgslayerOrtho);

        redraw();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    click.getAttributes().setOpacity(1.0);
                    click.setPosition(getCurrentPosition());
                }
            }
        });
    }
}

package org.jeo.map;

import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Disposable;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Compose data and styles for rendering.
 * <p>
 * A map maintains a collection of {@link Layer} objects containing the data composing the map. The
 * order of the layer objects in {@link #getLayers()} defines the rendering/drawing/z order for 
 * renderers. The {@link #getStyle()} contains the symbolization rules for the layers of the map.
 * </p>
 * <p>
 * A map has dimensions ({@link #getWidth()} and {@link #getHeight()}) in addition to a world 
 * extent specified by {@link #getBounds()}. THe bounds together with {@link #get    
 * </p>
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class Map implements Disposable {

    public static int DEFAULT_WIDTH = 256;
    public static int DEFAULT_HEIGHT = 256;
    
    int width = DEFAULT_WIDTH;
    int height = DEFAULT_HEIGHT;

    Envelope bounds = new Envelope(-180,180,-90,90);
    CoordinateReferenceSystem crs = Proj.EPSG_4326;
    
    List<Layer> layers = new ArrayList<Layer>();

    Stylesheet style = new Stylesheet();

    List<Disposable> cleanup = new ArrayList<Disposable>(); 

    public Map() {
    }

    /**
     * The width of the map in "rendering" units, usually pixels. 
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the map in "rendering" units, usually pixels. 
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * The height of the map in "rendering" units, usually pixels. 
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the map in "rendering" units, usually pixels. 
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * The spatial/world extent of the map.
     */
    public Envelope getBounds() {
        return bounds;
    }

    /**
     * Sets the spatial/world extent of the map.
     */
    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }

    /**
     * The projection / coordinate reference system of the map.
     */
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    /**
     * Sets the projection / coordinate reference system of the map.
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * The layers composing the map.
     */
    public List<Layer> getLayers() {
        return layers;
    }

    /**
     * The stylesheet containing the rules for rendering/symbolizing the map.  
     */
    public Stylesheet getStyle() {
        return style;
    }

    /**
     * Sets the stylesheet containing the rules for rendering/symbolizing the map.  
     */
    public void setStyle(Stylesheet style) {
        this.style = style;
    }

    /**
     * The horizontal scaling factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>map.width / bounds.width</pre>.
     */
    public double scaleX() {
        return getWidth() / bounds.getWidth();
    }

    /**
     * The vertical scaling factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>map.height / bounds.height</pre>.
     */
    public double scaleY() {
        return getHeight() / bounds.getHeight();
    }

    /**
     * The horizontal translation factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>-(bounds.minx * xscale)</pre>.
     */
    public double translateX() {
        return -bounds.getMinX() * scaleX();
    }

    /**
     * The vertical translation factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>bounds.miny * yscale + map.height</pre>.
     */
    public double translateY() {
        return (bounds.getMinY() * scaleY()) + getHeight();
    }

    /**
     * Disposable resources to dispose when the map is disposed. 
     */
    List<Disposable> getCleanup() {
        return cleanup;
    }

    @Override
    public void dispose() {
        for (Disposable d : cleanup) {
            d.dispose();
        }

        layers.clear();
    }
}
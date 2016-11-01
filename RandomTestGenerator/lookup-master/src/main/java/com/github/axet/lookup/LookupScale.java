package com.github.axet.lookup;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.axet.lookup.Lookup.NotFound;
import com.github.axet.lookup.common.GFirst;
import com.github.axet.lookup.common.GFirstLeftRight;
import com.github.axet.lookup.common.GPoint;
import com.github.axet.lookup.common.GSPoint;
import com.github.axet.lookup.common.ImageBinary;
import com.github.axet.lookup.common.ImageBinaryGreyScaleRGB;
import com.github.axet.lookup.common.ImageBinaryScale;
import com.github.axet.lookup.proc.NCC;

public class LookupScale {

    // minimum scale used
    public double scaleSize = 0;
    // blur kernel
    public int k;
    public float gg;
    public float g;

    /**
     * 
     * @param scaleSize
     *            ex:5
     * 
     * @param blurKernel
     *            ex:10
     * @param gg
     *            ex:0.70f
     * @param g
     *            ex:0.90f - for big templates, and 0.95f for small templates
     */
    public LookupScale(double scaleSize, int blurKernel, float gg, float g) {
        this.scaleSize = scaleSize;
        this.k = blurKernel;
        this.gg = gg;
        this.g = g;
    }

    public GSPoint lookup(BufferedImage i, BufferedImage t) {
        List<GSPoint> list = lookupAll(i, t);

        if (list.size() == 0)
            throw new NotFound();

        Collections.sort(list, new GFirst());

        return list.get(0);
    }

    public List<GSPoint> lookupAll(BufferedImage i, BufferedImage t) {
        ImageBinaryScale templateBinary = new ImageBinaryGreyScaleRGB(t);
        ImageBinaryScale imageBinary = new ImageBinaryGreyScaleRGB(i);

        return lookupAll(imageBinary, templateBinary);
    }

    public GSPoint lookup(ImageBinaryScale image, ImageBinaryScale template) {
        List<GSPoint> list = lookupAll(image, template);

        if (list.size() == 0)
            throw new NotFound();

        Collections.sort(list, new GFirst());

        return list.get(0);
    }

    public List<GSPoint> lookupAll(ImageBinaryScale image, ImageBinaryScale template) {
        scale(image, template);

        return lookupAll(image, 0, 0, image.image.getWidth() - 1, image.image.getHeight() - 1, template);
    }

    public GSPoint lookup(ImageBinaryScale image, int x1, int y1, int x2, int y2, ImageBinaryScale template) {
        scale(image, template);

        List<GSPoint> list = lookupAll(image, x1, y1, x2, y2, template);

        if (list.size() == 0)
            throw new NotFound();

        Collections.sort(list, new GFirst());

        return list.get(0);
    }

    public List<GSPoint> lookupAll(ImageBinaryScale image, int x1, int y1, int x2, int y2, ImageBinaryScale template) {
        scale(image, template);

        int sx1 = (int) (x1 * scaleSize);
        int sy1 = (int) (y1 * scaleSize);
        int sx2 = (int) (x2 * scaleSize);
        int sy2 = (int) (y2 * scaleSize);

        List<GSPoint> result = new ArrayList<GSPoint>();

        for (ImageBinary imageScaleBin : image.scales) {
            int ssy2 = sy2;
            int ssx2 = sx2;

            if (ssy2 >= imageScaleBin.getHeight())
                ssy2 = imageScaleBin.getHeight() - 1;
            if (ssx2 >= imageScaleBin.getWidth())
                ssx2 = imageScaleBin.getWidth() - 1;

            for (ImageBinary templateScaleBin : template.scales) {
                List<GPoint> list = NCC.lookupAll(imageScaleBin, sx1, sy1, ssx2, ssy2, templateScaleBin, gg);

                int mx = (int) (1 / scaleSize) * 2;
                int my = (int) (1 / scaleSize) * 2;

                for (GPoint p : list) {
                    Point p1 = new Point(p);

                    p1.x = (int) (p1.x / scaleSize - mx);
                    p1.y = (int) (p1.y / scaleSize - mx);

                    Point p2 = new Point(p1);
                    p2.x = template.image.getWidth() - 1 + p2.x + 2 * mx;
                    p2.y = template.image.getHeight() - 1 + p2.y + 2 * my;

                    if (p2.x >= image.image.getWidth())
                        p2.x = image.image.getWidth() - 1;
                    if (p2.y >= image.image.getHeight())
                        p2.y = image.image.getHeight() - 1;

                    List<GPoint> list2 = NCC.lookupAll(image.image, p1.x, p1.y, p2.x, p2.y, template.image, g);

                    for (GPoint g : list2) {
                        result.add(new GSPoint(g, p.g));
                    }
                }

                // we found something stop looking
                if (result.size() != 0)
                    break;
            }
        }

        // delete duplicates
        Collections.sort(result, new GFirstLeftRight(template.image));
        for (int k = 0; k < result.size(); k++) {
            GPoint kk = result.get(k);
            for (int j = k + 1; j < result.size(); j++) {
                GPoint jj = result.get(j);
                if (cross(template.image, kk, jj)) {
                    result.remove(jj);
                    j--;
                }
            }
        }

        for (GPoint p : result) {
            p.x += template.image.getWidth() / 2;
            p.y += template.image.getHeight() / 2;
        }

        return result;
    }

    boolean cross(ImageBinary image, GPoint i1, GPoint i2) {
        Rectangle r1 = new Rectangle(i1.x, i1.y, image.getWidth(), image.getHeight());
        Rectangle r2 = new Rectangle(i2.x, i2.y, image.getWidth(), image.getHeight());
        return r1.intersects(r2);
    }

    void scale(ImageBinaryScale image, ImageBinaryScale template) {
        if (scaleSize == 0) {
            scaleSize = template.s;
        }

        if (scaleSize != template.s) {
            template.rescaleMosaic(scaleSize, k);
        }

        if (scaleSize != image.s) {
            image.rescale(scaleSize, k);
        }
    }
}

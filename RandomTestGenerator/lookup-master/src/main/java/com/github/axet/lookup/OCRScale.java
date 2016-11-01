package com.github.axet.lookup;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.github.axet.lookup.common.FontSymbol;
import com.github.axet.lookup.common.FontSymbolLookup;
import com.github.axet.lookup.common.ImageBinary;
import com.github.axet.lookup.common.ImageBinaryGreyScale;
import com.github.axet.lookup.common.ImageBinaryScale;

/**
 * For big images you may want scale booth image and template image similarly. (lets say booth reduce by 2x, you will
 * still have same recognitionz quality but it goes twice faster on recognition step)
 * 
 * @author axet
 * 
 */
public class OCRScale extends OCR {

    public double scaleSize = 0;
    public int defaultBlurKernel;

    /**
     * 
     * @param scaleSize
     *            ex:5
     * 
     * @param blurKernel
     *            ex:10
     * @param threshold
     */
    public OCRScale(float scaleSize, int blurKernel, float threshold) {
        super(threshold);

        this.scaleSize = scaleSize;
        this.defaultBlurKernel = blurKernel;
    }

    public String recognize(BufferedImage bi) {
        ImageBinaryScale i = new ImageBinaryGreyScale(bi);

        return recognize(i);
    }

    public String recognize(ImageBinaryScale i) {
        List<FontSymbol> list = getSymbols();

        return recognize(i, 0, 0, i.image.getWidth() - 1, i.image.getHeight() - 1, list);
    }

    public String recognize(BufferedImage bi, String fontSet) {
        ImageBinaryScale i = new ImageBinaryGreyScale(bi);

        return recognize(i, fontSet);
    }

    public String recognize(ImageBinaryScale i, String fontSet) {
        List<FontSymbol> list = getSymbols(fontSet);

        return recognize(i, 0, 0, i.image.getWidth() - 1, i.image.getHeight() - 1, list);
    }

    public String recognize(ImageBinaryScale i, int x1, int y1, int x2, int y2) {
        List<FontSymbol> list = getSymbols();

        return recognize(i, x1, y1, x2, y2, list);
    }

    public String recognize(ImageBinaryScale i, int x1, int y1, int x2, int y2, String fontFamily) {
        List<FontSymbol> list = getSymbols(fontFamily);

        return recognize(i, x1, y1, x2, y2, list);
    }

    public String recognize(ImageBinaryScale i, int x1, int y1, int x2, int y2, List<FontSymbol> list) {
        for (FontSymbol s : list) {
            scale(i, s.image);
        }

        // before this point we operating on original image pixels. after it, we
        // are
        // operating on scaled coords

        x1 *= scaleSize;
        y1 *= scaleSize;
        x2 *= scaleSize;
        y2 *= scaleSize;

        List<FontSymbolLookup> all = new ArrayList<FontSymbolLookup>();

        for (ImageBinary iScaleBin : i.scales) {
            // rounding can be 1 pixels off images end
            if (x2 >= iScaleBin.getWidth())
                x2 = iScaleBin.getWidth() - 1;
            if (y2 >= iScaleBin.getHeight())
                y2 = iScaleBin.getHeight() - 1;

            all.addAll(findAll(list, iScaleBin, x1, y1, x2, y2));
        }

        return recognize(all);
    }

    public void scale(ImageBinaryScale image, ImageBinaryScale template) {
        if (scaleSize == 0) {
            scaleSize = template.s;
        }

        if (scaleSize != template.s) {
            template.rescaleMosaic(scaleSize, defaultBlurKernel);
        }

        if (scaleSize != image.s) {
            image.rescale(scaleSize, defaultBlurKernel);
        }
    }

}

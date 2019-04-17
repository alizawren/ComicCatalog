package com.alizawren.comiccatalog;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Alisa Ren on 4/16/2019.
 */

class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    //private GraphicOverlay mGraphicOverlay;

    //BarcodeTrackerFactory(GraphicOverlay graphicOverlay) {
    //    mGraphicOverlay = graphicOverlay;
    //}

    BarcodeTrackerFactory() {
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        //BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        //return new GraphicTracker<>(mGraphicOverlay, graphic);
        return new Tracker<Barcode>();
    }
}

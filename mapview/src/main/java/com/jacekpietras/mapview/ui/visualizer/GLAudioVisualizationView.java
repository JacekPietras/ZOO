package com.jacekpietras.mapview.ui.visualizer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Audio visualization view implementation for OpenGL.
 */
public class GLAudioVisualizationView extends GLSurfaceView implements AudioVisualization, InnerAudioVisualization {

    private static final int EGL_VERSION = 2;
    private final GLRenderer renderer;
    private DbmHandler<?> dbmHandler;
    private final Configuration configuration;
    private CalmDownListener innerCalmDownListener;

    private GLAudioVisualizationView(@NonNull Builder builder) {
        super(builder.context);
        configuration = new Configuration(builder);
        renderer = new GLRenderer(getContext(), configuration);
        init();
    }

    public GLAudioVisualizationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configuration = new Configuration(context, attrs, isInEditMode());
        renderer = new GLRenderer(getContext(), configuration);
        init();
    }

    private void init() {
        setEGLContextClientVersion(EGL_VERSION);
        setRenderer(renderer);
        renderer.calmDownListener(new CalmDownListener() {
            @Override
            public void onCalmedDown() {
                stopRendering();
                if (innerCalmDownListener != null) {
                    innerCalmDownListener.onCalmedDown();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dbmHandler != null) {
            dbmHandler.onResume();
        }
    }

    @Override
    public void onPause() {
        if (dbmHandler != null) {
            dbmHandler.onPause();
        }
        super.onPause();
    }

    @Override
    public <T> void linkTo(@NonNull DbmHandler<T> dbmHandler) {
        if (this.dbmHandler != null) {
            this.dbmHandler.release();
        }
        this.dbmHandler = dbmHandler;
        this.dbmHandler.setUp(this, configuration.layersCount);
    }

    @Override
    public void release() {
        if (dbmHandler != null) {
            dbmHandler.release();
            dbmHandler = null;
        }
    }

    @Override
    public void startRendering() {
        if (getRenderMode() != RENDERMODE_CONTINUOUSLY) {
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }
    }

    @Override
    public void stopRendering() {
        if (getRenderMode() != RENDERMODE_WHEN_DIRTY) {
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }
    }

    @Override
    public void calmDownListener(@Nullable CalmDownListener calmDownListener) {
        innerCalmDownListener = calmDownListener;
    }

    @Override
    public void onDataReceived(float[] dBmArray, float[] ampsArray) {
        renderer.onDataReceived(dBmArray, ampsArray);
    }

    /**
     * Configuration holder class.
     */
    static class Configuration {

        int wavesCount;
        int layersCount;
        int bubblesPerLayer;
        float bubbleSize;
        float waveHeight;
        float footerHeight;
        boolean randomizeBubbleSize;
        float[] backgroundColor;
        float[][] layerColors;

        public Configuration(Context context, AttributeSet attrs, boolean isInEditMode) {

        }

        private Configuration(@NonNull Builder builder) {
        }
    }

    public static class ColorsBuilder<T extends ColorsBuilder> {
        private float[] backgroundColor;
        private float[][] layerColors;
        private final Context context;

        public ColorsBuilder(@NonNull Context context) {
            this.context = context;
        }

        float[][] layerColors() {
            return layerColors;
        }

        float[] backgroundColor() {
            return backgroundColor;
        }

        /**
         * Set background color
         *
         * @param backgroundColor background color
         */
        public T setBackgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = Utils.convertColor(backgroundColor);
            return getThis();
        }

        /**
         * Set layer colors from array resource
         *
         * @param arrayId array resource
         */
        public T setLayerColors(@ArrayRes int arrayId) {
            TypedArray colorsArray = context.getResources().obtainTypedArray(arrayId);
            int[] colors = new int[colorsArray.length()];
            for (int i = 0; i < colorsArray.length(); i++) {
                colors[i] = colorsArray.getColor(i, Color.TRANSPARENT);
            }
            colorsArray.recycle();
            return setLayerColors(colors);
        }

        /**
         * Set layer colors.
         *
         * @param colors array of colors
         */
        public T setLayerColors(int[] colors) {
            layerColors = new float[colors.length][];
            for (int i = 0; i < colors.length; i++) {
                layerColors[i] = Utils.convertColor(colors[i]);
            }
            return getThis();
        }

        protected T getThis() {
            //noinspection unchecked
            return (T) this;
        }
    }

    public static class Builder extends ColorsBuilder<Builder> {

        private Context context;
        private int wavesCount;
        private int layersCount;
        private float bubbleSize;
        private float waveHeight;
        private float footerHeight;
        private boolean randomizeBubbleSize;
        private int bubblesPerLayer;

        public Builder(@NonNull Context context) {
            super(context);
            this.context = context;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        /**
         * Set waves count
         *
         * @param wavesCount waves count
         */
        public Builder setWavesCount(int wavesCount) {
            this.wavesCount = wavesCount;
            return this;
        }

        /**
         * Set layers count
         *
         * @param layersCount layers count
         */
        public Builder setLayersCount(int layersCount) {
            this.layersCount = layersCount;
            return this;
        }

        /**
         * Set bubbles size in pixels
         *
         * @param bubbleSize bubbles size in pixels
         */
        public Builder setBubblesSize(float bubbleSize) {
            this.bubbleSize = bubbleSize;
            return this;
        }

        /**
         * Set bubble size from dimension resource
         *
         * @param bubbleSize dimension resource
         */
        public Builder setBubblesSize(@DimenRes int bubbleSize) {
            return setBubblesSize((float) context.getResources().getDimensionPixelSize(bubbleSize));
        }

        /**
         * Set wave height in pixels
         *
         * @param waveHeight wave height in pixels
         */
        public Builder setWavesHeight(float waveHeight) {
            this.waveHeight = waveHeight;
            return this;
        }

        /**
         * Set wave height from dimension resource
         *
         * @param waveHeight dimension resource
         */
        public Builder setWavesHeight(@DimenRes int waveHeight) {
            return setWavesHeight((float) context.getResources().getDimensionPixelSize(waveHeight));
        }

        /**
         * Set footer height in pixels
         *
         * @param footerHeight footer height in pixels
         */
        public Builder setWavesFooterHeight(float footerHeight) {
            this.footerHeight = footerHeight;
            return this;
        }

        /**
         * Set footer height from dimension resource
         *
         * @param footerHeight dimension resource
         */
        public Builder setWavesFooterHeight(@DimenRes int footerHeight) {
            return setWavesFooterHeight((float) context.getResources().getDimensionPixelSize(footerHeight));
        }

        /**
         * Set flag indicates that size of bubbles should be randomized
         *
         * @param randomizeBubbleSize true if size of bubbles should be randomized, false if size of bubbles must be the same
         */
        public Builder setBubblesRandomizeSize(boolean randomizeBubbleSize) {
            this.randomizeBubbleSize = randomizeBubbleSize;
            return this;
        }

        /**
         * Set number of bubbles per layer.
         *
         * @param bubblesPerLayer number of bubbles per layer
         */
        public Builder setBubblesPerLayer(int bubblesPerLayer) {
            this.bubblesPerLayer = bubblesPerLayer;
            return this;
        }

        public GLAudioVisualizationView build() {
            return new GLAudioVisualizationView(this);
        }
    }

    /**
     * Renderer builder.
     */
    public static class RendererBuilder {

        private final Builder builder;
        private GLSurfaceView glSurfaceView;
        private DbmHandler handler;

        /**
         * Create new renderer using existing Audio Visualization builder.
         *
         * @param builder instance of Audio Visualization builder
         */
        public RendererBuilder(@NonNull Builder builder) {
            this.builder = builder;
        }

        /**
         * Set dBm handler.
         *
         * @param handler instance of dBm handler
         */
        public RendererBuilder handler(DbmHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Set OpenGL surface view.
         *
         * @param glSurfaceView instance of OpenGL surface view
         */
        public RendererBuilder glSurfaceView(@NonNull GLSurfaceView glSurfaceView) {
            this.glSurfaceView = glSurfaceView;
            return this;
        }

        /**
         * Create new Audio Visualization Renderer.
         *
         * @return new Audio Visualization Renderer
         */
        public AudioVisualizationRenderer build() {
            final GLRenderer renderer = new GLRenderer(builder.context, new Configuration(builder));
            final InnerAudioVisualization audioVisualization = new InnerAudioVisualization() {
                @Override
                public void startRendering() {
                    if (glSurfaceView.getRenderMode() != RENDERMODE_CONTINUOUSLY) {
                        glSurfaceView.setRenderMode(RENDERMODE_CONTINUOUSLY);
                    }
                }

                @Override
                public void stopRendering() {
                    if (glSurfaceView.getRenderMode() != RENDERMODE_WHEN_DIRTY) {
                        glSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
                    }
                }

                @Override
                public void calmDownListener(@Nullable CalmDownListener calmDownListener) {

                }

                @Override
                public void onDataReceived(float[] dBmArray, float[] ampsArray) {
                    renderer.onDataReceived(dBmArray, ampsArray);
                }
            };
            renderer.calmDownListener(new CalmDownListener() {
                @Override
                public void onCalmedDown() {
                    audioVisualization.stopRendering();
                }
            });
            handler.setUp(audioVisualization, builder.layersCount);
            return renderer;
        }
    }

    /**
     * Audio Visualization renderer interface that allows to change waves' colors at runtime.
     */
    public interface AudioVisualizationRenderer extends Renderer {

        /**
         * Update colors configuration.
         *
         * @param builder instance of color builder.
         */
        void updateConfiguration(@NonNull ColorsBuilder builder);
    }
}

package com.badlogic.skyfall;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Skyfall extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;



    @Override
    public void create() {

        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.viewport = new FitViewport(10,10);

        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        setScreen(new GameScreen(this));
    }


    @Override
    public void render(){super.render();}

    @Override
    public void dispose(){
        batch.dispose();
        font.dispose();
        screen.dispose();
    }
}

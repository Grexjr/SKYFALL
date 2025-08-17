package com.badlogic.skyfall;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private static final float MOVE_INTERVAL = 0.15f;
    private static final float BULLET_SPEED = 10f;
    private static final float BULLET_INTERVAL = 1f;

    final Skyfall game;
    final float worldWidth;
    final float worldHeight;


    Texture backgroundTexture, meteorTexture, playerTexture, bulletTexture;
    Sprite playerSprite;
    Array<Sprite> meteorSprites;
    Array<Sprite> bulletSprites;
    Rectangle playerRectangle;
    Rectangle meteorRectangle;
    Rectangle bulletRectangle;

    int score;
    boolean isGameOver;
    Array<Sprite> removeList;

    float dirX = 0f;
    float dirY = 0f;
    float moveTimer = 0f;
    float fallTimer = 0f;
    float bulletTimer = 0f;


    public GameScreen(Skyfall skyfall){
        this.game = skyfall;

        this.worldWidth = game.viewport.getWorldWidth();
        this.worldHeight = game.viewport.getWorldHeight();

        this.backgroundTexture = createPlaceholderTexture(Color.BLUE);
        this.meteorTexture = createPlaceholderTexture(Color.BROWN);
        this.playerTexture = createPlaceholderTexture(Color.GRAY);
        this.bulletTexture = createPlaceholderTexture(Color.BLACK);

        this.playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(1f,1f);

        this.bulletSprites = new Array<>();
        this.meteorSprites = new Array<>();
        this.removeList = new Array<>();

        this.playerRectangle = new Rectangle();
        this.meteorRectangle = new Rectangle();
        this.bulletRectangle = new Rectangle();
    }



    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        input();
        logic(delta);
        draw();
    }

    private void input(){
        if(!isGameOver){
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                dirX = 1; dirY = 0;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                dirX = -1; dirY = 0;
            } else if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
                if(bulletTimer > BULLET_INTERVAL) {
                    bulletTimer = 0f;
                    createBullet();
                }
            }
        }
    }

    private void logic(float delta){
        float fallInterval = Math.max(0.1f,
            0.5f - (float)(Math.pow(1.0009, 0.02 * score) - 1)
        );

        playerSprite.setX(MathUtils.clamp(playerSprite.getX(),0,worldWidth-playerSprite.getWidth()));
        playerRectangle.set(playerSprite.getX(), playerSprite.getY(), playerSprite.getWidth(),playerSprite.getHeight());

        if(!isGameOver) {
            moveTimer += delta;
            if (moveTimer > MOVE_INTERVAL) {
                moveTimer -= MOVE_INTERVAL;

                playerSprite.setPosition(
                    playerSprite.getX() + dirX,
                    playerSprite.getY() + dirY
                );

                dirX = 0;
                dirY = 0;
            }

            fallTimer += delta;
            if (fallTimer > fallInterval) {
                fallTimer -= fallInterval;

                int meteorNum = MathUtils.random(1, 6);
                for (int i = 0; i < meteorNum; i++) {
                    createMeteor();
                }

                for (int i = meteorSprites.size - 1; i >= 0; i--) {
                    Sprite meteorSprite = meteorSprites.get(i);
                    meteorSprite.setPosition(
                        meteorSprite.getX(),
                        meteorSprite.getY() - 1
                    );
                    meteorRectangle.set(meteorSprite.getX(), meteorSprite.getY(), meteorSprite.getWidth(), meteorSprite.getHeight());
                    if (meteorSprite.getY() < -worldHeight) meteorSprites.removeIndex(i);

                    else if (playerRectangle.overlaps(meteorRectangle)) {
                        isGameOver = true;
                        meteorSprites.removeIndex(i);
                    }
                }
            }

            bulletTimer += delta;
            for (int i = bulletSprites.size - 1; i >= 0; i--) {
                Sprite bulletSprite = bulletSprites.get(i);
                bulletSprite.translateY(BULLET_SPEED * delta);
                bulletRectangle.set(bulletSprite.getX(), bulletSprite.getY(), bulletSprite.getWidth(), bulletSprite.getHeight());
                if (bulletSprite.getY() > worldHeight) bulletSprites.removeIndex(i);
            }

            for (int i = meteorSprites.size - 1; i >= 0; i--) {
                if (!bulletSprites.isEmpty()) {
                    for (int j = bulletSprites.size - 1; j >= 0; j--) {
                        if (meteorSprites.get(i).getBoundingRectangle().overlaps(bulletSprites.get(j).getBoundingRectangle())) {
                            // adds the sprites to a remove list that is removed every frame to avoid iteration issues
                            removeList.add(meteorSprites.get(i));
                            removeList.add(bulletSprites.get(j));
                        }
                    }
                }
            }

            score++;
            meteorSprites.removeAll(removeList,true);
            bulletSprites.removeAll(removeList,true);

        }
    }

    private void createMeteor(){
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // Create drop sprite
        Sprite meteorSprite = new Sprite(meteorTexture);
        meteorSprite.setSize(dropWidth,dropHeight);
        meteorSprite.setX(MathUtils.round(MathUtils.random(0f, worldWidth - dropWidth))); // randomize drop position, clamped to screen
        meteorSprite.setY(worldHeight);
        meteorSprites.add(meteorSprite); // add it to the list
    }

    private void createBullet(){
        Sprite bulletSprite = new Sprite(bulletTexture);
        bulletSprite.setSize(0.5f,1f);
        bulletSprite.setX(playerSprite.getX() + (playerSprite.getWidth()/4));
        bulletSprite.setY(playerSprite.getY());
        bulletSprites.add(bulletSprite);
    }

    private void draw(){
        ScreenUtils.clear(Color.BLACK);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        game.batch.draw(backgroundTexture,0,0,worldWidth,worldHeight);
        playerSprite.draw(game.batch);

        for(Sprite meteor : meteorSprites){
            meteor.draw(game.batch);
        }

        for(Sprite bullet : bulletSprites){
            bullet.draw(game.batch);
        }

        game.font.draw(game.batch,Integer.toString(score),0,worldHeight);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        game.viewport.update(width,height,true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        backgroundTexture.dispose();
        playerTexture.dispose();
        meteorTexture.dispose();
        bulletTexture.dispose();
    }

    private Texture createPlaceholderTexture(Color color){
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}

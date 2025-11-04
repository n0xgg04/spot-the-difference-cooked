package com.ltm.game.client.services;

import javafx.scene.media.AudioClip;

public class AudioService {
    private AudioClip backgroundMusic;
    private AudioClip gameMusic;
    private AudioClip correctSound;
    private AudioClip wrongSound;
    private AudioClip celebrationSound;

    public void playBackgroundMusic() {
        stopBackgroundMusic();
        try {
            String musicPath = getClass().getResource("/sounds/y_ke_que.mp3").toExternalForm();
            backgroundMusic = new AudioClip(musicPath);
            backgroundMusic.setCycleCount(AudioClip.INDEFINITE);
            backgroundMusic.setVolume(0.3);
            backgroundMusic.play();
            System.out.println("Playing background music: " + musicPath);
        } catch (Exception e) {
            System.out.println("Could not load background music: " + e.getMessage());
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void playGameMusic() {
        stopGameMusic();
        try {
            String musicPath = getClass().getResource("/sounds/nhac_ingame.mp3").toExternalForm();
            gameMusic = new AudioClip(musicPath);
            gameMusic.setCycleCount(AudioClip.INDEFINITE);
            gameMusic.setVolume(0.25);
            gameMusic.play();
            System.out.println("Playing game music: " + musicPath);
        } catch (Exception e) {
            System.out.println("Could not load game music: " + e.getMessage());
        }
    }

    public void stopGameMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
        }
    }

    public void loadGameSounds() {
        try {
            String correctPath = getClass().getResource("/sounds/ye_đoan_dung_roi.mp3").toExternalForm();
            correctSound = new AudioClip(correctPath);
            correctSound.setVolume(0.6);
            System.out.println("Loaded correct sound: " + correctPath);
        } catch (Exception e) {
            System.out.println("Could not load correct sound: " + e.getMessage());
        }

        try {
            String wrongPath = getClass().getResource("/sounds/phai_chiu.mp3").toExternalForm();
            wrongSound = new AudioClip(wrongPath);
            wrongSound.setVolume(0.5);
            System.out.println("Loaded wrong sound: " + wrongPath);
        } catch (Exception e) {
            System.out.println("Could not load wrong sound: " + e.getMessage());
        }
    }

    public void playCorrectSound() {
        if (correctSound != null) {
            correctSound.play();
        }
    }

    public void playWrongSound() {
        if (wrongSound != null) {
            wrongSound.play();
        }
    }

    public void playCelebrationSound() {
        try {
            String soundPath = getClass().getResource("/sounds/ving_quang.mp3").toExternalForm();
            celebrationSound = new AudioClip(soundPath);
            celebrationSound.setVolume(0.2);
            celebrationSound.play();
            System.out.println("Playing celebration sound: " + soundPath);
        } catch (Exception e) {
            System.err.println("Error playing celebration sound: " + e.getMessage());
        }
    }

    public void stopAll() {
        stopBackgroundMusic();
        stopGameMusic();
    }
}


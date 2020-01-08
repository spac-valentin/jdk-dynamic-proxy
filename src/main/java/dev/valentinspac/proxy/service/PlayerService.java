package dev.valentinspac.proxy.service;

import dev.valentinspac.proxy.framework.MyCustomTransaction;
import dev.valentinspac.proxy.framework.TransactionalService;

import java.io.File;

@TransactionalService
public class PlayerService implements Playable, Seekable {

    @Override
    @MyCustomTransaction
    public String play(String filePath) {
        return this.play(new File(filePath));
    }

    @Override
    @MyCustomTransaction("file")
    public String play(File song) {
        // Increment in DB number of plays for this song
        return "Playing song " + song.getName();
    }

    @Override
    @MyCustomTransaction("starting at")
    public String play(File song, int start) {
        // Increment in DB number of plays for this song
        return "Playing song " + song.getName() + " starting at " + start;
    }

    @Override
    @MyCustomTransaction("interval")
    public String play(File song, int start, int finish) {
        // Increment in DB number of plays for this song
        return "Playing song " + song.getName() + " starting at " + start + " till " + finish;
    }

    @Override
    public String seekTo(int position) {
        return "Seek to " + position;
    }
}


package whitman.cs370proj.composer;

public class Note{
    private int pitch;
    private int tick;
    private int duration;
    public int channel;

    public Note(int p, int t, int c, int d){
        pitch = p;
        tick = t;
        duration = d;
        channel = c;
    }

    public int getPitch(){
        return pitch;
    }

    public void setPitch(int p){
        this.pitch = p;
    }

    public int getTick(){
        return tick;
    }

    public void setTick(int t){
        this.tick = t;
    }

    public int getDuration(){
        return duration;
    }

    public void setDuration(int d){
        this.duration = d;
    }

    public int getChannel(){
        return channel;
    }

    public void setChannel(int c){
        this.channel = c;
    }
}


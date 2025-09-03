package psdk.EventEditor.model;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    private String name;
    private int x;
    private int y;
    private List<EventPage> pages;

    public Event(int id, String name, int x, int y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.pages = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<EventPage> getPages() {
        return pages;
    }

    public void addPage(EventPage page) {
        this.pages.add(page);
    }

    public void setPages(List<EventPage> pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "Event [id=" + id + ", name=" + name + ", x=" + x + ", y=" + y + ", pages=" + pages.size() + "]";
    }
}
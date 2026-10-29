package www.almato.com.pojo;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    List<Item> menu;
    String lstKeEncrpt;
    boolean canQueryForMore;

    public Menu() {
        menu = new ArrayList<>();
    }

    public void addItem(Item item) {
        menu.add(item);
    }

    public boolean isCanQueryForMore() {
        return canQueryForMore;
    }

    public void setCanQueryForMore(boolean canQueryForMore) {
        this.canQueryForMore = canQueryForMore;
    }

    public String getLstKeEncrpt() {
        return lstKeEncrpt;
    }

    public void setLstKeEncrpt(String lstKeEncrpt) {
        this.lstKeEncrpt = lstKeEncrpt;
    }

    public List<Item> getMenu() {
        return menu;
    }

    public void setMenu(List<Item> menu) {
        this.menu = menu;
    }
}

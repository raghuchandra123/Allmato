package www.almato.com.pojo;

public class Item {
    String item;
    Integer price;

    public Item(String _item, Integer _price) {
        item = _item;
        price = _price;
    }

    public Item() {
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}

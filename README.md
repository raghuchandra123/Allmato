# Zomato – Food Ordering System

## Functional Requirements

* Customers and restaurants can register with the application.

* Each restaurant has:

  * Opening time and closing time
  * Working days
  * A menu

* Each menu contains multiple items.

* Each menu item has:

  * Name
  * Price
  * Description
  * Quantity in grams
  * Veg / Non-veg
  * Batch size
  * Preparation time per batch

* Customers can search for a particular food item and see:

  * Restaurants that serve the item
  * Price of the item at each restaurant
  * Estimated time to prepare **one quantity** of the item

---

## POINT OF FOCUS: How should we handle orders?

The main challenge is to make sure that a restaurant does not receive more orders than it can reasonably process, while also avoiding unnecessary restrictions for customers.

### Approach 1: Restaurant-level limit

We can set a limit on the number of pending orders that a restaurant can have at a time.

For example:

```text
Maximum pending orders = 10
```

Once the restaurant has 10 pending orders, we do not allow any new orders until some of the existing orders are completed.

**Pros:**

* Simple to implement.
* Prevents a large buildup of orders.
* Ensures that the restaurant does not receive more orders than it can handle.

**Cons:**

* From the customer's perspective, this can be frustrating.
* The customer may see that the restaurant is not accepting orders and has to wait until it becomes available again.
* The restaurant may lose customers who are ready to place an order.

---

### Approach 2: Item-level limit

Instead of setting a limit for the entire restaurant, we can set limits for individual items.

For example:

```text
Pizza → maximum 20 pending orders
Idly  → maximum 100 pending orders
```

Once the limit for an item is reached, we do not accept any more orders for that item.

Other items can still remain available.

**Pros:**

* More flexible than setting a limit for the entire restaurant.
* The restaurant does not have to become completely unavailable.
* Items that still have capacity can continue accepting orders.

**Cons:**

* The restaurant can still lose customers for items that have reached their limit.
* A customer may repeatedly see that a particular item, such as pizza, is unavailable.
* Managing capacity becomes more complex when an order contains multiple different items.

---

### Approach 3: Estimate ETA for every order

Instead of simply rejecting orders when a limit is reached, we estimate the ETA for each new order.

The ETA can be calculated based on factors such as:

* Existing pending orders
* Items in the new order
* Preparation time of the items
* Restaurant's current workload

We show the estimated time to the customer.

The customer can then decide whether the ETA is acceptable and place the order.

For example:

```text
Pizza
Price: ₹300
Estimated preparation/delivery time: 45 minutes
```

The customer can decide whether they are willing to wait 45 minutes before placing the order.

**Pros:**

* The restaurant does not unnecessarily reject customers.
* Customers can decide based on the expected waiting time.
* The restaurant can continue accepting orders even when it is busy, as long as the estimated time is communicated to the customer.

**Cons:**

* ETA is only an estimate and can never be completely accurate.
* We need to make sure that the actual time does not differ significantly from the estimated time.

For example, suppose the restaurant currently has no pending orders. We may initially estimate an ETA of 10 minutes.

If 1,000 orders arrive at almost the same time, the actual preparation time could become much higher.

This situation is unlikely during normal times, but it can happen during exceptional periods such as New Year's Eve or other major events. In such situations, restaurants would ideally prepare for the expected increase in demand.

---

## Order Flow

Once the customer decides to place the order:

```text
Customer places order
        ↓
Customer makes payment
        ↓
Payment successful
        ↓
Order is confirmed
        ↓
Restaurant prepares the order
        ↓
Order is packed
        ↓
Order is ready for delivery
        ↓
Order is delivered
```

The customer should be able to check the **current status of the order at any time**.

---

## Out of Scope

The following functionalities are out of scope for this design:

* Delivery process
* Delivery-partner assignment
* Distance-related functionality
* Location-based restaurant filtering

  * For example, showing only restaurants within a 5 km radius
* Reviews
* Returns / refund-related functionality

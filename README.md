# Allmato — Fair Restaurant Food Ordering

Allmato is a food ordering application designed to make restaurant business more equitable. Instead of allowing a small number of restaurants to capture most of the orders, Allmato aims to distribute business fairly by considering restaurant ratings, order-processing capacity, and the revenue each restaurant has generated during the day.

The goal is simple: **help restaurants receive a fair share of business while ensuring that customer orders can be fulfilled reliably.**

## 📱 Application Preview

<!-- Add your application screenshot to the repository at docs/images/allmato.png -->

![Allmato food ordering application](docs/images/allmato.png)

The application displays a paginated menu of food items with their prices. Users can select quantities, view the total amount, and place an order.

## 🔄 How Order Processing Works

### 1. Paginated Menu Loading

The Android application fetches menu items from the DynamoDB `CombinedMenu` table through the Java backend.

- The initial API request retrieves a page of menu items.
- As the user scrolls toward the bottom of the list, the application requests the next page.
- Pagination continues until no more items are available.
- ViewModel-based state management helps preserve the menu and selected quantities across configuration changes and background transitions.

This avoids loading the entire menu into the application at once.

### 2. Order Submission

When the user presses the **Order** button:

1. The Android application calls the Java order API.
2. The backend attempts to publish an order message to Kafka.
3. The API returns an immediate response indicating whether the order was accepted for processing or the submission failed.

The API response acknowledges submission; restaurant allocation happens asynchronously and may take additional time.

### 3. Parallel Order Processing

Multiple instances of the order-processing service subscribe to Kafka using a consumer group.

- Kafka distributes partitions among the available consumers.
- Different workers can process messages concurrently, subject to partition availability.
- The workers independently execute the restaurant-selection and allocation workflow.

This separates order submission from potentially expensive allocation work and allows the processing layer to scale.

### 4. Fair Restaurant Selection

For each incoming order, the processing service evaluates restaurant information stored in DynamoDB.

The selection considers:

- **Daily revenue:** How much business the restaurant has generated during the current day.
- **Customer ratings:** The restaurant's review and rating information.
- **Processing capacity:** How quickly the restaurant can process orders and whether it can fulfil the requested items.

The intention is to favour restaurants that deserve more business while respecting their operational capacity.

### 5. Redis-Based Restaurant Locking

Because multiple workers may process orders simultaneously, concurrent allocation could otherwise assign work to the same restaurant beyond the intended allocation limits.

Redis is used to coordinate restaurant allocation:

1. A worker attempts to acquire a lock for a candidate restaurant.
2. If the restaurant is already being processed by another worker, the worker avoids that restaurant and considers another candidate.
3. Once the lock is acquired, the worker checks whether the restaurant can fulfil the requested order.
4. If it can fulfil the order, the worker allocates the order to that restaurant.
5. If it can fulfil only part of the order, the remaining items are considered for further allocation.
6. The lock is released when the protected allocation operation finishes.

This coordination helps prevent conflicting allocations across concurrent workers.

### 6. Allocation Completion Notification

Once the order has been fully allocated, a notification is sent back to the application so the user can be informed of the allocation result.

This separates the initial order-submission response from the eventual allocation-completion notification.

## 🛡️ UI State and Failure Handling

The Android application uses ViewModels and Jetpack Compose state management to keep the UI consistent during normal application lifecycle events.

- **Configuration changes:** Preserve menu and quantity-selection state across device rotation.
- **Background and foreground transitions:** Retain UI state while the application moves between foreground and background.
- **API failures:** Handle backend unavailability through failure handling and logging rather than allowing an unhandled failure to crash the application.
- **Asynchronous operations:** Keep network requests and other long-running work separate from direct UI rendering.

## 🚀 Design Principles

Allmato is built around a few core architectural principles:

- **Fairness:** Restaurant selection considers daily business, ratings, and capacity.
- **Separation of concerns:** The Android UI, backend APIs, and asynchronous processing have distinct responsibilities.
- **Concurrency:** Kafka consumer groups enable parallel message processing.
- **Coordination:** Redis helps workers coordinate access to restaurant allocation.
- **Incremental loading:** Paginated menu retrieval avoids fetching all menu items upfront.
- **Lifecycle-aware UI:** ViewModel-based state management supports a more robust Android experience.

## 🌱 Vision

Allmato explores a different approach to food ordering: one where restaurant discovery and order allocation are designed not only around customer demand, but also around sustainable business distribution.

By combining asynchronous processing, distributed coordination, and capacity-aware restaurant selection, Allmato aims to help prevent smaller or newly opened restaurants from being overlooked while still considering ratings and the ability to fulfil orders.

package gg.loot.lootfairness;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

@Route("game")
public class GamePriceView extends VerticalLayout implements BeforeEnterObserver {


    private final Label priceLabel = new Label();
    private final Label itemIdLabel = new Label();

    private final Label pageTitle = new Label("Loot Fairness Verifier");
    private final TextField publicSeedField = new TextField("Public Seed");
    private final TextField clientSeedField = new TextField("Client Seed");
    private final TextField serverSeedField = new TextField("Server Seed");
    private final TextField boxIdField = new TextField("Box Id");
    private final Button getItemButton = new Button("Get Item");


    Icon githubIcon = new Icon(VaadinIcon.CODE);

    Button githubButton = new Button("View Source Code on GitHub", githubIcon);

    public GamePriceView() throws NoSuchAlgorithmException {
        pageTitle.getStyle().set("font-size", "24px"); // optional: change the font-size of title
        pageTitle.getStyle().set("font-weight", "bold"); // optional: make the title bold

        setBoxSizing(BoxSizing.BORDER_BOX);
        setJustifyContentMode(JustifyContentMode.CENTER);

        pageTitle.getStyle().set("margin-bottom", "20px"); // add padding-bottom to title

        VerticalLayout container = new VerticalLayout();
        this.setAlignItems(Alignment.CENTER);
        add(pageTitle, container);

        container.add(publicSeedField, clientSeedField, serverSeedField, boxIdField, getItemButton, priceLabel, itemIdLabel);

        container.add(githubButton);

        container.getElement().getStyle().set("max-width", "600px");

        container.setAlignItems(Alignment.CENTER);
        container.setWidth("80%");

        publicSeedField.setWidthFull();
        clientSeedField.setWidthFull();
        serverSeedField.setWidthFull();
        boxIdField.setWidthFull();
        getItemButton.setWidthFull();


        getItemButton.addClickListener(event -> generateItem());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        publicSeedField.setValue(event.getLocation().getQueryParameters().getParameters().getOrDefault("publicSeed", List.of("")).get(0));
        clientSeedField.setValue(event.getLocation().getQueryParameters().getParameters().getOrDefault("clientSeed", List.of("")).get(0));
        serverSeedField.setValue(event.getLocation().getQueryParameters().getParameters().getOrDefault("serverSeed", List.of("")).get(0));
        boxIdField.setValue(event.getLocation().getQueryParameters().getParameters().getOrDefault("boxId", List.of("")).get(0));

        githubButton.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs("window.open('https://github.com/lootgg/fairness', '_blank')");
        });
    }

    private void generateItem() {

        try {

            SecureRandom random = Utils.getSecureRandom(publicSeedField.getValue(), serverSeedField.getValue(), clientSeedField.getValue());
            String publicSeed = publicSeedField.getValue();
            String clientSeed = clientSeedField.getValue();
            String serverSeed = serverSeedField.getValue();
            String boxId = boxIdField.getValue();

            if (publicSeed.isEmpty() || clientSeed.isEmpty() || serverSeed.isEmpty() || boxId.isEmpty()) {
                Notification notification = Notification.show(
                        "All fields are required", 5000, Notification.Position.MIDDLE);
                return;
            }

            String url = String.format("game?publicSeed=%s&clientSeed=%s&serverSeed=%s&boxId=%s",
                    publicSeed, clientSeed, serverSeed, boxId);
            UI.getCurrent().navigate(url);

            final var box = Utils.getBoxById(boxId);

            var item = Utils.generateItemWon(random, box.getItems());
            priceLabel.setText("You won " + item.getName() + " with a price of " + item.getPrice());
            itemIdLabel.setText("Item id: " + item.getId());
        } catch (Exception e) {
            e.printStackTrace();
            Notification notification = Notification.show(
                    "Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    @Data
    @NoArgsConstructor
    public static class Box {
        private String id;
        private String name;
        private String slug;
        private String description;
        private List<ItemDetail> items;
        private double price;
        private double houseEdge;
        private String imageUrl;
        private boolean active;
    }


    @Data
    @NoArgsConstructor
    public static class ItemDetail {
        private Item item;
        private double frequency;

        @Data
        @NoArgsConstructor
        public static class Item {
            private String id;
            private String name;
            private String slug;
            private String description;
            private double price;
            private boolean isSellable;
            private boolean isDeleted;
            private boolean isInStock;
            private String type;
        }

    }
}
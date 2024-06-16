package ru.alexander.vtscupcounter;

import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import ru.alexander.api.listeners.ResponseListener;
import ru.alexander.api.responses.ErrorResponse;
import ru.alexander.api.values.FadeMode;
import ru.alexander.api.values.ItemTransform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class AppController {
    public CupCounterApplication application;

    public Settings settings;
    private final Random random = new Random();
    private long cupPlacementSeed = random.nextLong();
    public final List<String> filesList = new ArrayList<>();
    private final LinkedList<Item> instances = new LinkedList<>();

    @FXML
    public ListView<String> items;
    @FXML
    public TextField minCupStackSizeField;
    @FXML
    public TextField maxCupStackSizeField;
    @FXML
    public TextField cupStackDistance;
    @FXML
    private TextField count;
    @FXML
    public TextField pixelScaleField;
    @FXML
    public TextField orderField;
    @FXML
    public Slider anchorX;
    @FXML
    public Slider anchorY;
    @FXML
    public Slider sizeSlider;
    @FXML
    public Slider randomnessSlider;

    public void start() {
        count.setText(String.valueOf(0));
        pixelScaleField.setText(String.valueOf(settings.pixelScale));
        orderField.setText(String.valueOf(settings.order));
        cupStackDistance.setText(String.valueOf(settings.cupStackDistance));
        minCupStackSizeField.setText(String.valueOf(settings.minCupStackSize));
        maxCupStackSizeField.setText(String.valueOf(settings.maxCupStackSize));

        anchorX.setValue(settings.x);
        anchorY.setValue(settings.y);
        sizeSlider.setValue(settings.size);
        randomnessSlider.setValue(settings.maxAngle);
    }

    @FXML
    private void onAddVariantClick() {
        List<File> file = application.showOpenMultipleDialog();
        if (file == null) return;
        file.forEach(f -> settings.files.add(f.getAbsolutePath()));
        filesList.clear();
        filesList.addAll(settings.files);
        items.setItems(new ObservableListBase<>() {
            @Override
            public String get(int index) {
                String name = new File(filesList.get(index)).getName();
                return name.substring(0, name.lastIndexOf("."));
            }

            @Override
            public int size() {
                return filesList.size();
            }
        });

        saveSettings();
    }
    public void onListMenuRequested(ContextMenuEvent contextMenuEvent) {

    }

    public void onListKeyPressed(KeyEvent keyEvent) {

    }

    @FXML
    private void onRandomnessChanged() {
        settings.maxAngle = (float) randomnessSlider.getValue();
        saveSettings();
        updateTransform();
    }
    @FXML
    private void onPixelScaleChanged() {
        try {
            settings.pixelScale = Float.parseFloat(pixelScaleField.getText());
            saveSettings();
            updateTransform();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
    @FXML
    private void onXChanged() {
        settings.x = (float) anchorX.getValue();
        saveSettings();
        updateTransform();
    }
    @FXML
    private void onYChanged() {
        settings.y = (float) anchorY.getValue();
        saveSettings();
        updateTransform();
    }

    @FXML
    public void onSizeChanged() {
        settings.size = (float) sizeSlider.getValue();
        saveSettings();
        updateTransform();
    }

    @FXML
    public void onOrderChange() {
        try {
            settings.order = Integer.parseInt(orderField.getText());
            saveSettings();
            updateTransform();
        } catch (Exception e) {
            System.err.println(e);
        }
    }


    @FXML
    private void onChangeCount() {
        try {
            int c = Integer.parseInt(count.getText());

            AtomicReference<Runnable> runnable = new AtomicReference<>();
            if (instances.size() > c) {
                runnable.set(() -> {
                    if (instances.size() > c) decrement(runnable.get());
                });
            }
            if (instances.size() < c) {
                runnable.set(() -> {
                    if (instances.size() < c) increment(runnable.get());
                });
            }
            if (runnable.get() != null)
                runnable.get().run();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
    @FXML
    private void onIncrementClick() {
        increment(null);
    }
    @FXML
    private void onDecrementClick() {
        decrement(null);
    }

    public void onMinCupStackSizeChanged() {
        try {
            settings.minCupStackSize = Integer.parseInt(minCupStackSizeField.getText());
            saveSettings();
            updateTransform();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    public void onMaxCupStackSizeChanged() {
        try {
            settings.maxCupStackSize = Integer.parseInt(maxCupStackSizeField.getText());
            saveSettings();
            updateTransform();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void onDistanceChange() {
        try {
            settings.cupStackDistance = Float.parseFloat(cupStackDistance.getText());
            saveSettings();
            updateTransform();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void increment(Runnable listener) {
        int i = random.nextInt(settings.files.size());
        String path = filesList.get(i);

        try {
            Image image = new Image(new FileInputStream(path));
            float r = random.nextFloat() * 2 - 1;

            int stacks = 0;
            int stackSize = 0;
            float maxWidth = 0;
            AtomicReference<Float> localX = new AtomicReference<>(settings.x);
            AtomicReference<Float> localY = new AtomicReference<>(settings.y);
            AtomicReference<Float> localRotation = new AtomicReference<>(0f);
            Random rand = new Random(cupPlacementSeed);
            for (Item item : instances) {
                double rot = Math.toRadians(localRotation.get() * settings.maxAngle);
                float h = item.height * settings.pixelScale * settings.size;
                localX.updateAndGet(v -> (float) (v + h * Math.sin(rot)));
                localY.updateAndGet(v -> (float) (v + h * Math.cos(rot)));
                localRotation.updateAndGet(v -> v + item.randomAngle);
                stackSize++;

                maxWidth = Float.max(maxWidth, item.width);
                if (stackSize >= rand.nextInt(settings.minCupStackSize, settings.maxCupStackSize + 1)) {
                    stacks++;
                    localX.set(settings.x + maxWidth * settings.pixelScale * settings.cupStackDistance * stacks);
                    localY.set(settings.y);
                    localRotation.set(0f);
                    stackSize = 0;
                    maxWidth = 0;
                }
            }

            application.getAPI().loadCustomItem(
                    new FileInputStream(path),
                    false,
                    false,
                    -1,
                    new File(path).getName(),
                    localX.get(),
                    localY.get(),
                    settings.size,
                    localRotation.get() * settings.maxAngle,
                    0.1f,
                    settings.order,
                    false,
                    0,
                    false,
                    false,
                    true,
                    true,
                    new ResponseListener<>() {
                        @Override
                        public void onSuccess(LinkedHashMap<String, Object> map) {
                            instances.addLast(new Item((String) map.get("instanceID"), r,
                                    (float) image.getWidth(),
                                    (float) image.getHeight()));
                            System.out.println("Item placed!");
                            count.setText(String.valueOf(instances.size()));
                            if (listener != null) listener.run();
                        }

                        @Override
                        public void onFailure(ErrorResponse errorResponse) {
                            System.err.println(errorResponse);
                        }
                    }
            );
        } catch (IOException e) {
            settings.files.remove(path);
            filesList.remove(i);
            System.out.println("File loading error!");
        }
    }
    private void decrement(Runnable listener) {
        if (instances.isEmpty()) return;
        Item item = instances.pollLast();
        application.getAPI().removeItems(
                List.of(item.id),
                null,
                true,
                new ResponseListener<>() {
                    @Override
                    public void onSuccess(LinkedHashMap<String, Object> stringObjectLinkedHashMap) {
                        System.out.println("Item removed!");
                        if (listener != null) listener.run();
                    }

                    @Override
                    public void onFailure(ErrorResponse errorResponse) {
                        System.err.println(errorResponse);
                    }
                }
        );
        count.setText(String.valueOf(instances.size()));
    }

    private void updateTransform() {
        ArrayList<ItemTransform> transforms = new ArrayList<>();
        AtomicReference<Float> localX = new AtomicReference<>(settings.x);
        AtomicReference<Float> localY = new AtomicReference<>(settings.y);
        AtomicReference<Float> localRotation = new AtomicReference<>(0f);

        int stacks = 0;
        int stackSize = 0;
        float maxWidth = 0;
        cupPlacementSeed = random.nextLong();
        Random rand = new Random(cupPlacementSeed);
        for (Item item : instances) {
            double rot = Math.toRadians(localRotation.get() * settings.maxAngle);
            transforms.add(new ItemTransform(
                    item.id,
                    0.1f,
                    FadeMode.EaseBoth,
                    localX.get(),
                    localY.get(),
                    settings.size,
                    localRotation.get() * settings.maxAngle,
                    settings.order + transforms.size(),
                    false
            ));
            float h = item.height * settings.pixelScale * settings.size;
            localX.updateAndGet(v -> (float) (v + h * Math.sin(rot)));
            localY.updateAndGet(v -> (float) (v + h * Math.cos(rot)));
            localRotation.updateAndGet(v -> v + item.randomAngle);
            stackSize++;

            maxWidth = Float.max(maxWidth, item.width);
            if (stackSize >= rand.nextInt(settings.minCupStackSize, settings.maxCupStackSize + 1)) {
                stacks++;
                localX.set(settings.x + maxWidth * settings.pixelScale * settings.cupStackDistance * stacks);
                localY.set(settings.y);
                localRotation.set(0f);
                maxWidth = 0;
                stackSize = 0;
            }
        }
        application.getAPI().moveItems(transforms, new ResponseListener<>() {
            @Override
            public void onSuccess(LinkedHashMap<String, Object> stringObjectLinkedHashMap) {
                System.out.println("Items moved!");
            }

            @Override
            public void onFailure(ErrorResponse errorResponse) {
                System.err.println(errorResponse);
            }
        });
    }

    public void saveSettings() {
        try {
            FileWriter writer = new FileWriter("settings");
            writer.write(application.getGson().toJson(settings));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record Item(String id, float randomAngle, float width, float height) {
    }
}
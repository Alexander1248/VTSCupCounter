package ru.alexander.vtscupcounter;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

public class Settings {
    public float maxAngle = 0;
    public float x = 0;
    public float y = 0;
    public float size = 0.3f;
    public float pixelScale = 0.00055f;
    public int order = 10;
    public int minCupStackSize = 7;
    public int maxCupStackSize = 7;
    public float cupStackDistance = 1.5f;
    public final Set<String> files = new TreeSet<>();
}

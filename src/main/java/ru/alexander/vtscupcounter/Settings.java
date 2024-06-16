package ru.alexander.vtscupcounter;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

public class Settings {
    public float maxAngle = 4;
    public float x = 0.7108436f;
    public float y = -0.6746987f;
    public float size = 0.3f;
    public float pixelScale = 0.00055f;
    public int order = 10;
    public int minCupStackSize = 4;
    public int maxCupStackSize = 7;
    public float cupStackDistance = -0.1f;
    public final Set<String> files = new TreeSet<>();
}

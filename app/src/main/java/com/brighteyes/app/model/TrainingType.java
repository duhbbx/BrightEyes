package com.brighteyes.app.model;

public enum TrainingType {
    RED_FLASH("红光闪烁", "通过红光刺激增强视网膜敏感度"),
    GRATING("光栅训练", "使用条纹图案刺激视觉皮层"),
    TRACKING("追踪训练", "追踪移动目标锻炼眼肌协调"),
    FOCUS("聚焦训练", "远近聚焦训练调节能力"),
    COLOR_RECOGNITION("色彩识别", "增强色彩辨识能力"),
    SHAPE_MATCHING("形状配对", "提高图形识别与记忆");

    private final String displayName;
    private final String description;

    TrainingType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

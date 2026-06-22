package com.example.reportfrontapi.domain.category;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 카테고리 색상 생성 유틸. 채도(S)/명도(V)는 고정하고 색상(hue)만 분산시켜
 * 카테고리 수 제한 없이 서로 구분되는 색을 만든다. 저장 포맷은 "#RRGGBB".
 */
public final class CategoryColor {
    // 기존 프론트 팔레트 톤(부드러운 중채도)에 맞춘 고정 채도/명도.
    private static final float SATURATION = 0.5f;
    private static final float VALUE = 0.82f;
    // 기존 색과 최소 이만큼(도)은 떨어지도록 시도한다.
    private static final float MIN_HUE_GAP = 25f;
    private static final int MAX_TRIES = 24;

    private CategoryColor() {
    }

    /**
     * 이미 사용 중인 색(hex)들과 최대한 겹치지 않는 새 색을 만든다.
     * 랜덤 hue를 뽑되 기존 hue들과 MIN_HUE_GAP 이상 떨어진 후보를 우선하고,
     * 못 찾으면 기존과 가장 먼 후보를 채택한다.
     */
    public static String randomAvoiding(List<String> existingHexColors) {
        List<Float> hues = existingHexColors.stream()
                .map(CategoryColor::hueOf)
                .filter(h -> !Float.isNaN(h))
                .toList();
        float[] usedHues = new float[hues.size()];
        for (int i = 0; i < hues.size(); i++) {
            usedHues[i] = hues.get(i);
        }

        float best = ThreadLocalRandom.current().nextFloat() * 360f;
        float bestGap = -1f;
        for (int i = 0; i < MAX_TRIES; i++) {
            float candidate = ThreadLocalRandom.current().nextFloat() * 360f;
            float gap = minHueDistance(candidate, usedHues);
            if (gap >= MIN_HUE_GAP) {
                return hexOf(candidate);
            }
            if (gap > bestGap) {
                bestGap = gap;
                best = candidate;
            }
        }
        return hexOf(best);
    }

    // hue(도) → "#RRGGBB"
    private static String hexOf(float hueDegrees) {
        int rgb = Color.HSBtoRGB(hueDegrees / 360f, SATURATION, VALUE) & 0xFFFFFF;
        return String.format("#%06X", rgb);
    }

    // "#RRGGBB" → hue(도). 파싱 불가 시 NaN.
    private static float hueOf(String hex) {
        if (hex == null || hex.length() != 7 || hex.charAt(0) != '#') {
            return Float.NaN;
        }
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            float[] hsb = Color.RGBtoHSB(r, g, b, null);
            return hsb[0] * 360f;
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }

    // 후보 hue와 사용 중 hue들 사이의 최소 원형 거리(도). 사용 중이 없으면 360(최대).
    private static float minHueDistance(float candidate, float[] usedHues) {
        if (usedHues.length == 0) {
            return 360f;
        }
        float min = 360f;
        for (float used : usedHues) {
            float diff = Math.abs(candidate - used) % 360f;
            float dist = Math.min(diff, 360f - diff);
            min = Math.min(min, dist);
        }
        return min;
    }
}

package com.amigo.navi.keyguard.skylight;

import android.text.format.Time;

public final class TimeDefinition {

    public static enum TimeSection {
        DAY, EVENING, NIGHT
    };
    
    private TimeDefinition() {
    }
    
    private static final class Holder {
        private static final TimeDefinition INSTANCE = new TimeDefinition();
    }
    
    public static TimeDefinition obtain() {
        return Holder.INSTANCE;
    }
    
    public boolean isDayTime(boolean containsEvening) {
        final TimeSection section = Definition.queryTime();
        return containsEvening ? (section.equals(TimeSection.DAY) || section.equals(TimeSection.EVENING))
                : (section.equals(TimeSection.DAY));
    }
    
    public boolean isDayTime() {
        return isDayTime(false);
    }
    
    public boolean isDayTimeContainsEvening() {
        return isDayTime(true);
    }
    
    public boolean isMorning() {
        return Definition.isMorning();
    }
    
    public boolean isNight() {
        final TimeSection section = Definition.queryTime();
        return section.equals(TimeSection.NIGHT);
    }
    
    public boolean isEvening() {
        final TimeSection section = Definition.queryTime();
        return section.equals(TimeSection.EVENING);
    }
    
    public TimeSection getNowTimeSection() {
        return Definition.queryTime();
    }
    
    private static class Definition {
        private static Time mTime;
        private static final int SLOT_1 = 6;
        private static final int SLOT_2 = 16;
        private static final int SLOT_3 = 17;
        private static final int SLOT_4 = 19;
        
        //add by chenng (morning time is 6 - 12)
        private static final int SLOT_5 = 11;

        private Definition() {
        }

        private static void resetTime() {
            if (mTime == null) {
                mTime = new Time();
            }
            mTime.setToNow();
        }
        
        public static boolean isMorning() {
            resetTime();
            return mTime.hour >= SLOT_1 && mTime.hour < SLOT_5;
        }
        
        public static TimeSection queryTime() {
            resetTime();
            
            if (mTime.hour <= SLOT_2 && mTime.hour >= SLOT_1) {
                return TimeSection.DAY;
            } else if (mTime.hour >= SLOT_3 && mTime.hour < SLOT_4) {
                return TimeSection.EVENING;
            }
            
            return TimeSection.NIGHT;
        }
    }
}

    package com.example.lab7_ex2;

    public class MediaItem {

        private long id;
        private String name;
        private int mediaType;
        private boolean isSelected;

        public MediaItem(long id, String name, int mediaType) {
            this.id = id;
            this.name = name;
            this.mediaType = mediaType;
            this.isSelected = false;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getMediaType() {
            return mediaType;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

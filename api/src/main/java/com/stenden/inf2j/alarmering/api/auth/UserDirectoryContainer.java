package com.stenden.inf2j.alarmering.api.auth;

public final class UserDirectoryContainer implements Comparable<UserDirectoryContainer> {

    private final int id;
    private final int priority;
    private final UserDirectory directory;

    public UserDirectoryContainer(int id, int priority, UserDirectory directory) {
        if(directory == null){
            throw new NullPointerException("directory");
        }

        this.id = id;
        this.priority = priority;
        this.directory = directory;
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public UserDirectory getDirectory() {
        return directory;
    }

    @Override
    public int compareTo(UserDirectoryContainer o) {
        return Integer.compare(o.priority, this.priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDirectoryContainer that = (UserDirectoryContainer) o;

        if (id != that.id) return false;
        if (priority != that.priority) return false;
        return directory.equals(that.directory);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + priority;
        result = 31 * result + directory.hashCode();
        return result;
    }
}

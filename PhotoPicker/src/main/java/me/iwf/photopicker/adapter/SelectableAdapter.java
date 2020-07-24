package me.iwf.photopicker.adapter;


import android.os.Build;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.entity.PhotoDirectory;
import me.iwf.photopicker.event.Selectable;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Selectable {

    private static final String TAG = SelectableAdapter.class.getSimpleName();

    protected List<PhotoDirectory> photoDirectories;
    protected List<String> selectedPhotosPath;
    protected List<Integer> selectedPhotosId;

    public int currentDirectoryIndex = 0;


    public SelectableAdapter() {
        photoDirectories = new ArrayList<>();
        selectedPhotosPath = new ArrayList<>();
        selectedPhotosId = new ArrayList<>();
    }


    /**
     * Indicates if the item at position where is selected
     *
     * @param photo Photo of the item to check
     * @return true if the item is selected, false otherwise
     */
    @Override
    public boolean isSelected(Photo photo) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            return getSelectedPhotosId().contains(photo.getId());
        else
            return getSelectedPhotosPath().contains(photo.getPath());
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param photo Photo of the item to toggle the selection status for
     */
    @Override
    public void toggleSelection(Photo photo) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

            if (selectedPhotosId.contains(photo.getId())) {
                for (int i = 0; i < selectedPhotosId.size(); i++) {
                    if (selectedPhotosId.get(i) == photo.getId()) {
                        selectedPhotosId.remove(i);
                        break;
                    }
                }
                //selectedPhotosId.remove(photo.getId());
            } else {
                selectedPhotosId.add(photo.getId());
            }
        } else {
            if (selectedPhotosPath.contains(photo.getPath())) {
                selectedPhotosPath.remove(photo.getPath());
            } else {
                selectedPhotosPath.add(photo.getPath());
            }
        }
    }


    /**
     * Clear the selection status for all items
     */
    @Override
    public void clearSelection() {
        selectedPhotosPath.clear();
        selectedPhotosId.clear();
    }


    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    @Override
    public int getSelectedItemCount() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return selectedPhotosId.size();
        } else {
            return selectedPhotosPath.size();
        }
    }


    public void setCurrentDirectoryIndex(int currentDirectoryIndex) {
        this.currentDirectoryIndex = currentDirectoryIndex;
    }


    public List<Photo> getCurrentPhotos() {
        if (photoDirectories.size() <= currentDirectoryIndex) {
            currentDirectoryIndex = photoDirectories.size() - 1;
        }
        return photoDirectories.get(currentDirectoryIndex).getPhotos();
    }


    public List<String> getCurrentPhotoPaths() {
        List<String> currentPhotoPaths = new ArrayList<>(getCurrentPhotos().size());
        for (Photo photo : getCurrentPhotos()) {
            currentPhotoPaths.add(photo.getPath());
        }
        return currentPhotoPaths;
    }

    public List<Integer> getCurrentPhotoIDs() {
        List<Integer> currentPhotoIDs = new ArrayList<>(getCurrentPhotos().size());
        for (Photo photo : getCurrentPhotos()) {
            currentPhotoIDs.add(photo.getId());
        }
        return currentPhotoIDs;
    }


    public List<String> getSelectedPhotosPath() {
        return selectedPhotosPath;
    }

    public List<Integer> getSelectedPhotosId() {
        return selectedPhotosId;
    }

}
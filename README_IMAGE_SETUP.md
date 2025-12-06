# Adding Puzzle Images

## Quick Setup

To add the test panda image (or any other puzzle images):

### Option 1: Using Assets Folder (Recommended)

1. In Android Studio, right-click on `app/src/main/` folder
2. Select `New` > `Directory`
3. Choose `assets` (if it doesn't exist)
4. Right-click on `assets` folder
5. Select `New` > `Directory`
6. Name it `puzzles`
7. Copy your `test_panda.jpg` image into `app/src/main/assets/puzzles/` folder

### Option 2: Using Drawable Resources

1. Copy `test_panda.jpg` to `app/src/main/res/drawable/` folder
2. Rename it to `test_panda.jpg` (or `puzzle_panda.jpg` - no uppercase letters)
3. The app will automatically use it

## Image Requirements

- Format: JPG or PNG
- Recommended size: 1024x1024 pixels (square)
- The app will automatically crop non-square images to square

## Current Status

The app currently falls back to a colorful test pattern if no images are found in the assets folder.
Once you add images to `app/src/main/assets/puzzles/`, they will be loaded automatically.

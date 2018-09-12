public void myMethod() {
    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath); // load
    
    //preview image
    bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);
    Uri bitmapURI = bitmapToUriConverter(bitmap);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    byte[] imageInByte = baos.toByteArray();
    String saveThis = Base64.encodeToString(imageInByte, Base64.NO_WRAP);

    SharedPreferences preferences = getSharedPreferences("PREFS_PHOTO", 0);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString("sh_image", saveThis);
    editor.apply();

    iv_profile_dialog.setImageBitmap(bitmap);
    iv_profile_main.setImageBitmap(bitmap);
    Uri uri = Uri.fromFile(new File(selectedImagePath));
    retrofitAddPicture(bitmapURI);
}

                public Bitmap resizeImageForImageView(Bitmap bitmap) {
                    Bitmap resizedBitmap;
                    int scaleSize = 500;
            
                    int originalWidth = bitmap.getWidth();
                    int originalHeight = bitmap.getHeight();
                    int newWidth = -1;
                    int newHeight = -1;
                    float multFactor = -1.0F;
                    if (originalHeight > originalWidth) {
                        newHeight = scaleSize;
                        multFactor = (float) originalWidth / (float) originalHeight;
                        newWidth = (int) (newHeight * multFactor);
                    } else if (originalWidth > originalHeight) {
                        newWidth = scaleSize;
                        multFactor = (float) originalHeight / (float) originalWidth;
                        newHeight = (int) (newWidth * multFactor);
                    } else if (originalHeight == originalWidth) {
                        newHeight = scaleSize;
                        newWidth = scaleSize;
                    }
                    resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
                    return resizedBitmap;
                }

                private Uri bitmapToUriConverter(Bitmap mBitmap) {
                    Uri uri = null;
            //        int size_width = 400;
            //        int size_height = 400;
                    try {
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        // Calculate inSampleSize
                        //options.inSampleSize = calculateInSampleSize(options, size_width, size_height);
            
                        // Decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;
                        //Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, size_width, size_height, true);
                        File file = new File(this.getFilesDir(), "Image"
                                + new Random().nextInt() + ".jpeg");
                        FileOutputStream out = this.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        //get absolute path
                        String realPath = file.getAbsolutePath();
                        File f = new File(realPath);
                        uri = Uri.fromFile(f);
            
                    } catch (Exception e) {
                        Log.e("Your Error Message", e.getMessage());
                    }
                    return uri;
                }

                public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
                    // Raw height and width of image
                    final int height = options.outHeight;
                    final int width = options.outWidth;
                    int inSampleSize = 1;
            
                    if (height > reqHeight || width > reqWidth) {
            
                        final int halfHeight = height / 2;
                        final int halfWidth = width / 2;
            
                        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                        // height and width larger than the requested height and width.
                        while ((halfHeight / inSampleSize) >= reqHeight
                                && (halfWidth / inSampleSize) >= reqWidth) {
                            inSampleSize *= 2;
                        }
                    }
            
                    return inSampleSize;
                }

                
//                int rotate = 0;
//                ExifInterface exif;
//                if (Build.VERSION.SDK_INT > 23) {
//                    assert input != null;
//                    exif = new ExifInterface(input);
//                } else {
//                    exif = new ExifInterface(f.getAbsolutePath());
//
//                    int orientation = exif.getAttributeInt(
//                            ExifInterface.TAG_ORIENTATION,
//                            ExifInterface.ORIENTATION_NORMAL);
//
//                    switch (orientation) {
//                        case ExifInterface.ORIENTATION_ROTATE_270:
//                            rotate = 270;
//                            break;
//                        case ExifInterface.ORIENTATION_ROTATE_180:
//                            rotate = 180;
//                            break;
//                        case ExifInterface.ORIENTATION_ROTATE_90:
//                            rotate = 90;
//                            break;
//                    }
//                }
//                Matrix matrix = new Matrix();
//                matrix.postRotate(rotate);
//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//                        bitmap.getHeight(), matrix, true);

//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] imageInByte = baos.toByteArray();
//                String saveThis = Base64.encodeToString(imageInByte, Base64.NO_WRAP);
//
//                SharedPreferences preferences = getSharedPreferences("PREFS_PHOTO", 0);
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putString("sh_image", saveThis);
//                editor.apply();



               .listener(new RequestListener<Bitmap>() {
                   @Override
                   public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {

                       return false;
                   }

                   @Override
                   public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                       ByteArrayOutputStream baos = new ByteArrayOutputStream();
                       resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                       byte[] imageInByte = baos.toByteArray();
                       String saveThis = Base64.encodeToString(imageInByte, Base64.NO_WRAP);

                       Log.e(TAG, "onResourceReady: " + s_url_image);
                       Log.e(TAG, "onResourceReady: " + sh_url_image);

                       if (!sh_url_image.equals(s_url_image) || sh_url_image.isEmpty()) {

                           SharedPreferences preferences = getSharedPreferences("PREFS_PHOTO", 0);
                           SharedPreferences.Editor editor = preferences.edit();
                           editor.putString("sh_url_image", s_url_image);
                           editor.putString("sh_image", saveThis);
                           editor.apply();

                           SharedPreferences settings = getSharedPreferences("PREFS_PHOTO", 0);
                           sh_image = settings.getString("sh_image", "");
                           sh_url_image = settings.getString("sh_url_image", "");
                           byte_image = Base64.decode(sh_image, Base64.NO_WRAP);

                           Bitmap bmp = BitmapFactory.decodeByteArray(byte_image, 0, byte_image.length);
                           ivProfile.setImageBitmap(bmp);
                       }

                       return false;
                   }
               }).submit();
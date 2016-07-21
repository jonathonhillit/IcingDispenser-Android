package nz.ac.aut.engineering.icingprinter;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Jonathon Hill <xyc8034@aut.ac.nz>
 *                           ID: 1301787
 * This class handles the transformation of the image data into the data
 * representation suitable for the cake printer
 */
public class ImageLib {

    public static Colour3[] palette = new Colour3[]{
            new Colour3(1, 1, 1),
            new Colour3(255, 255, 255)

    };

    // Floyd-Steinberg Dithering
    public static Bitmap fsDither(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Colour3[][] d = new Colour3[h][w];

        // Save the pixel data to an array
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new Colour3(bitmap.getPixel(x, y));
            }
        }

        // Iterate over the image
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {

                Colour3 oldColor = d[y][x];
                Colour3 newColor = findClosetColour(oldColor, palette);
                bitmap.setPixel(x, y, newColor.toColor());

                Colour3 err = oldColor.sub(newColor);

                // Distribute the Error
                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(6. / 16));
                }
                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(2. / 16));
                }
                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(4. / 16));
                }
                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
                }
            }
        }
        return bitmap;
    }

    // Static method that performs Jarvis-Judice-Ninke Dithering
    public static Bitmap jjnDither(Bitmap bitmap) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Colour3[][] d = new Colour3[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new Colour3(bitmap.getPixel(x, y));
            }
        }

        ////// FLOYD-STEINBERG BEGINS HERE
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {

                Colour3 oldColor = d[y][x];
                Colour3 newColor = findClosetColour(oldColor, palette);
                bitmap.setPixel(x, y, newColor.toColor());

                Colour3 err = oldColor.sub(newColor);

                // OLD VERSION OF ALGORITHM
//                // Top row
//                if (x + 1 < w) {
//                    d[y][x + 1] = d[y][x + 1].add(err.mul(7. / 48));
//                }
//                if (x + 2 < w) {
//                    d[y][x + 2] = d[y][x + 2].add(err.mul(5. / 48));
//                }
//
//                // Middle row
//                if (x - 2 >= 0 && y + 1 < h) {
//                    d[y + 1][x - 2] = d[y + 1][x - 2].add(err.mul(3. / 48));
//                }
//                if (x - 1 >= 0 && y + 1 < h) {
//                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(5. / 48));
//                }
//                if (y + 1 < h) {
//                    d[y + 1][x] = d[y + 1][x].add(err.mul(7. / 48));
//                }
//                if (x + 1 < w && y + 1 < h) {
//                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(5. / 48));
//                }
//                if (x + 2 < w && y + 1 < h) {
//                    d[y + 1][x + 2] = d[y + 1][x + 2].add(err.mul(3. / 48));
//                }
//
//                // Bottom row
//                if (x - 2 >= 0 && y + 2 < h) {
//                    d[y + 2][x - 2] = d[y + 2][x - 2].add(err.mul(1. / 48));
//                }
//                if (x - 1 >= 0 && y + 2 < h) {
//                    d[y + 2][x - 1] = d[y + 2][x - 1].add(err.mul(3. / 48));
//                }
//                if (y + 2 < h) {
//                    d[y + 2][x] = d[y + 2][x].add(err.mul(5. / 48));
//                }
//                if (x + 1 < w && y + 2 < h) {
//                    d[y + 2][x + 1] = d[y + 2][x + 1].add(err.mul(3. / 48));
//                }
//                if (x + 2 < w && y + 2 < h) {
//                    d[y + 2][x + 2] = d[y + 2][x + 2].add(err.mul(1. / 48));
//                }

                // Top row
                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(6. / 48));
                }
                if (x + 2 < w) {
                    d[y][x + 2] = d[y][x + 2].add(err.mul(4. / 48));
                }

                // Middle row
                if (x - 2 >= 0 && y + 1 < h) {
                    d[y + 1][x - 2] = d[y + 1][x - 2].add(err.mul(2. / 48));
                }
                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(4. / 48));
                }
                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(6. / 48));
                }
                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(5. / 48));
                }
                if (x + 2 < w && y + 1 < h) {
                    d[y + 1][x + 2] = d[y + 1][x + 2].add(err.mul(2. / 48));
                }

                // Bottom row
                if (x - 2 >= 0 && y + 2 < h) {
                    d[y + 2][x - 2] = d[y + 2][x - 2].add(err.mul(1. / 48));
                }
                if (x - 1 >= 0 && y + 2 < h) {
                    d[y + 2][x - 1] = d[y + 2][x - 1].add(err.mul(2. / 48));
                }
                if (y + 2 < h) {
                    d[y + 2][x] = d[y + 2][x].add(err.mul(4. / 48));
                }
                if (x + 1 < w && y + 2 < h) {
                    d[y + 2][x + 1] = d[y + 2][x + 1].add(err.mul(2. / 48));
                }
                if (x + 2 < w && y + 2 < h) {
                    d[y + 2][x + 2] = d[y + 2][x + 2].add(err.mul(1. / 48));
                }
            }
        }
        return bitmap;
    }

    // Static method that performs Atkinson Dithering
    public static Bitmap atkDither(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Colour3[][] d = new Colour3[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new Colour3(bitmap.getPixel(x, y));
            }
        }

        ////// FLOYD-STEINBERG BEGINS HERE
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {

                Colour3 oldColor = d[y][x];
                Colour3 newColor = findClosetColour(oldColor, palette);
                bitmap.setPixel(x, y, newColor.toColor());

                Colour3 err = oldColor.sub(newColor);

                // Top row
                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(1. / 8));
                }
                if (x + 2 < w) {
                    d[y][x + 2] = d[y][x + 2].add(err.mul(1. / 8));
                }

                // Middle row

                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(1. / 8));
                }
                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(1. / 8));
                }
                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 8));
                }


                // Bottom row


                if (y + 2 < h) {
                    d[y + 2][x] = d[y + 2][x].add(err.mul(1. / 8));
                }
            }
        }
        return bitmap;
    }

    // Static method that performs Sierra Dithering
    public static Bitmap sDither(Bitmap bitmap) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Colour3[][] d = new Colour3[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new Colour3(bitmap.getPixel(x, y));
            }
        }

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {

                Colour3 oldColor = d[y][x];
                Colour3 newColor = findClosetColour(oldColor, palette);
                bitmap.setPixel(x, y, newColor.toColor());

                Colour3 err = oldColor.sub(newColor);

//                // Top row
//                if (x + 1 < w) {
//                    d[y][x + 1] = d[y][x + 1].add(err.mul(5. / 32));
//                }
//                if (x + 2 < w) {
//                    d[y][x + 2] = d[y][x + 2].add(err.mul(3. / 32));
//                }
//
//                // Middle row
//                if (x - 2 >= 0 && y + 1 < h) {
//                    d[y + 1][x - 2] = d[y + 1][x - 2].add(err.mul(2. / 32));
//                }
//                if (x - 1 >= 0 && y + 1 < h) {
//                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(4. / 32));
//                }
//                if (y + 1 < h) {
//                    d[y + 1][x] = d[y + 1][x].add(err.mul(5. / 32));
//                }
//                if (x + 1 < w && y + 1 < h) {
//                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(4. / 32));
//                }
//                if (x + 2 < w && y + 1 < h) {
//                    d[y + 1][x + 2] = d[y + 1][x + 2].add(err.mul(2. / 32));
//                }
//
//                // Bottom row
//
//                if (x - 1 >= 0 && y + 2 < h) {
//                    d[y + 2][x - 1] = d[y + 2][x - 1].add(err.mul(2. / 32));
//                }
//                if (y + 2 < h) {
//                    d[y + 2][x] = d[y + 2][x].add(err.mul(3. / 32));
//                }
//                if (x + 1 < w && y + 2 < h) {
//                    d[y + 2][x + 1] = d[y + 2][x + 1].add(err.mul(2. / 32));
//                }

                // Top row
                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(4. / 32));
                }
                if (x + 2 < w) {
                    d[y][x + 2] = d[y][x + 2].add(err.mul(2. / 32));
                }

                // Middle row
                if (x - 2 >= 0 && y + 1 < h) {
                    d[y + 1][x - 2] = d[y + 1][x - 2].add(err.mul(1. / 32));
                }
                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 32));
                }
                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(4. / 32));
                }
                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(3. / 32));
                }
                if (x + 2 < w && y + 1 < h) {
                    d[y + 1][x + 2] = d[y + 1][x + 2].add(err.mul(1. / 32));
                }

                // Bottom row

                if (x - 1 >= 0 && y + 2 < h) {
                    d[y + 2][x - 1] = d[y + 2][x - 1].add(err.mul(1. / 32));
                }
                if (y + 2 < h) {
                    d[y + 2][x] = d[y + 2][x].add(err.mul(2. / 32));
                }
                if (x + 1 < w && y + 2 < h) {
                    d[y + 2][x + 1] = d[y + 2][x + 1].add(err.mul(1. / 32));
                }
            }
        }
        return bitmap;
    }

    private static Colour3 findClosetColour(Colour3 c, Colour3[] palette) {
        Colour3 closest = palette[0];

        for (Colour3 n : palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n;
            }
        }

        return closest;
    }

    static class Colour3 {
        int r, g, b, a;

        public Colour3(int c) {
            //this.a = Color.alpha(c);
            this.r = Color.red(c);
            this.g = Color.green(c);
            this.b = Color.blue(c);

        }

        public Colour3(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public Colour3 add(Colour3 o) {
            return new Colour3(r + o.r, g + o.g, b + o.b);
        }
My
        public Colour3 sub(Colour3 o) {
            return new Colour3(r - o.r, g - o.g, b - o.b);
        }

        public Colour3 mul(double d) {
            return new Colour3((int) (d * r), (int) (d * g), (int) (d * b));
        }

        public int diff(Colour3 o) {
            return Math.abs(r - o.r) + Math.abs(g - o.g) + Math.abs(b - o.b);
        }

        public int toRGB() {

            return toColor();
        }

        public int toColor() {
            return Color.rgb(clamp(r), clamp(g), clamp(b));
        }

        public int clamp(int c) {
            return Math.max(0, Math.min(255, c));
        }
    }
}
/*
 * Copyright 2012-2014 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.values.Color;

public abstract class SvgColor {
  public static final SvgColor ALICE_BLUE = new SvgColorKeyword("aliceblue");
  public static final SvgColor ANTIQUE_WHITE = new SvgColorKeyword("antiquewhite");
  public static final SvgColor AQUA = new SvgColorKeyword("aqua");
  public static final SvgColor AQUAMARINE = new SvgColorKeyword("aquamarine");
  public static final SvgColor AZURE = new SvgColorKeyword("azure");
  public static final SvgColor BEIGE = new SvgColorKeyword("beige");
  public static final SvgColor BISQUE = new SvgColorKeyword("bisque");
  public static final SvgColor BLACK = new SvgColorKeyword("black");
  public static final SvgColor BLANCHED_ALMOND = new SvgColorKeyword("blanchedalmond");
  public static final SvgColor BLUE = new SvgColorKeyword("blue");
  public static final SvgColor BLUE_VIOLET = new SvgColorKeyword("blueviolet");
  public static final SvgColor BROWN = new SvgColorKeyword("brown");
  public static final SvgColor BURLY_WOOD = new SvgColorKeyword("burlywood");
  public static final SvgColor CADET_BLUE = new SvgColorKeyword("cadetblue");
  public static final SvgColor CHARTREUSE = new SvgColorKeyword("chartreuse");
  public static final SvgColor CHOCOLATE = new SvgColorKeyword("chocolate");
  public static final SvgColor CORAL = new SvgColorKeyword("coral");
  public static final SvgColor CORNFLOWER_BLUE = new SvgColorKeyword("cornflowerblue");
  public static final SvgColor CORNSILK = new SvgColorKeyword("cornsilk");
  public static final SvgColor CRIMSON = new SvgColorKeyword("crimson");
  public static final SvgColor CYAN = new SvgColorKeyword("cyan");
  public static final SvgColor DARK_BLUE = new SvgColorKeyword("darkblue");
  public static final SvgColor DARK_CYAN = new SvgColorKeyword("darkcyan");
  public static final SvgColor DARK_GOLDEN_ROD = new SvgColorKeyword("darkgoldenrod");
  public static final SvgColor DARK_GRAY = new SvgColorKeyword("darkgray");
  public static final SvgColor DARK_GREEN = new SvgColorKeyword("darkgreen");
  public static final SvgColor DARK_GREY = new SvgColorKeyword("darkgrey");
  public static final SvgColor DARK_KHAKI = new SvgColorKeyword("darkkhaki");
  public static final SvgColor DARK_MAGENTA = new SvgColorKeyword("darkmagenta");
  public static final SvgColor DARK_OLIVE_GREEN = new SvgColorKeyword("darkolivegreen");
  public static final SvgColor DARK_ORANGE = new SvgColorKeyword("darkorange");
  public static final SvgColor DARK_ORCHID = new SvgColorKeyword("darkorchid");
  public static final SvgColor DARK_RED = new SvgColorKeyword("darkred");
  public static final SvgColor DARK_SALMON = new SvgColorKeyword("darksalmon");
  public static final SvgColor DARK_SEA_GREEN = new SvgColorKeyword("darkseagreen");
  public static final SvgColor DARK_SLATE_BLUE = new SvgColorKeyword("darkslateblue");
  public static final SvgColor DARK_SLATE_GRAY = new SvgColorKeyword("darkslategray");
  public static final SvgColor DARK_SLATE_GREY = new SvgColorKeyword("darkslategrey");
  public static final SvgColor DARK_TURQUOISE = new SvgColorKeyword("darkturquoise");
  public static final SvgColor DARK_VIOLET = new SvgColorKeyword("darkviolet");
  public static final SvgColor DEEP_PINK = new SvgColorKeyword("deeppink");
  public static final SvgColor DEEP_SKY_BLUE = new SvgColorKeyword("deepskyblue");
  public static final SvgColor DIM_GRAY = new SvgColorKeyword("dimgray");
  public static final SvgColor DIM_GREY = new SvgColorKeyword("dimgrey");
  public static final SvgColor DODGER_BLUE = new SvgColorKeyword("dodgerblue");
  public static final SvgColor FIRE_BRICK = new SvgColorKeyword("firebrick");
  public static final SvgColor FLORAL_WHITE = new SvgColorKeyword("floralwhite");
  public static final SvgColor FOREST_GREEN = new SvgColorKeyword("forestgreen");
  public static final SvgColor FUCHSIA = new SvgColorKeyword("fuchsia");
  public static final SvgColor GAINSBORO = new SvgColorKeyword("gainsboro");
  public static final SvgColor GHOST_WHITE = new SvgColorKeyword("ghostwhite");
  public static final SvgColor GOLD = new SvgColorKeyword("gold");
  public static final SvgColor GOLDEN_ROD = new SvgColorKeyword("goldenrod");
  public static final SvgColor GRAY = new SvgColorKeyword("gray");
  public static final SvgColor GREY = new SvgColorKeyword("grey");
  public static final SvgColor GREEN = new SvgColorKeyword("green");
  public static final SvgColor GREEN_YELLOW = new SvgColorKeyword("greenyellow");
  public static final SvgColor HONEY_DEW = new SvgColorKeyword("honeydew");
  public static final SvgColor HOT_PINK = new SvgColorKeyword("hotpink");
  public static final SvgColor INDIAN_RED = new SvgColorKeyword("indianred");
  public static final SvgColor INDIGO = new SvgColorKeyword("indigo");
  public static final SvgColor IVORY = new SvgColorKeyword("ivory");
  public static final SvgColor KHAKI = new SvgColorKeyword("khaki");
  public static final SvgColor LAVENDER = new SvgColorKeyword("lavender");
  public static final SvgColor LAVENDER_BLUSH = new SvgColorKeyword("lavenderblush");
  public static final SvgColor LAWN_GREEN = new SvgColorKeyword("lawngreen");
  public static final SvgColor LEMON_CHIFFON = new SvgColorKeyword("lemonchiffon");
  public static final SvgColor LIGHT_BLUE = new SvgColorKeyword("lightblue");
  public static final SvgColor LIGHT_CORAL = new SvgColorKeyword("lightcoral");
  public static final SvgColor LIGHT_CYAN = new SvgColorKeyword("lightcyan");
  public static final SvgColor LIGHT_GOLDEN_ROD_YELLOW = new SvgColorKeyword("lightgoldenrodyellow");
  public static final SvgColor LIGHT_GRAY = new SvgColorKeyword("lightgray");
  public static final SvgColor LIGHT_GREEN = new SvgColorKeyword("lightgreen");
  public static final SvgColor LIGHT_GREY = new SvgColorKeyword("lightgrey");
  public static final SvgColor LIGHT_PINK = new SvgColorKeyword("lightpink");
  public static final SvgColor LIGHT_SALMON = new SvgColorKeyword("lightsalmon");
  public static final SvgColor LIGHT_SEA_GREEN = new SvgColorKeyword("lightseagreen");
  public static final SvgColor LIGHT_SKY_BLUE = new SvgColorKeyword("lightskyblue");
  public static final SvgColor LIGHT_SLATE_GRAY = new SvgColorKeyword("lightslategray");
  public static final SvgColor LIGHT_SLATE_GREY = new SvgColorKeyword("lightslategrey");
  public static final SvgColor LIGHT_STEEL_BLUE = new SvgColorKeyword("lightsteelblue");
  public static final SvgColor LIGHT_YELLOW = new SvgColorKeyword("lightyellow");
  public static final SvgColor LIME = new SvgColorKeyword("lime");
  public static final SvgColor LIME_GREEN = new SvgColorKeyword("limegreen");
  public static final SvgColor LINEN = new SvgColorKeyword("linen");
  public static final SvgColor MAGENTA = new SvgColorKeyword("magenta");
  public static final SvgColor MAROON = new SvgColorKeyword("maroon");
  public static final SvgColor MEDIUM_AQUA_MARINE = new SvgColorKeyword("mediumaquamarine");
  public static final SvgColor MEDIUM_BLUE = new SvgColorKeyword("mediumblue");
  public static final SvgColor MEDIUM_ORCHID = new SvgColorKeyword("mediumorchid");
  public static final SvgColor MEDIUM_PURPLE = new SvgColorKeyword("mediumpurple");
  public static final SvgColor MEDIUM_SEAGREEN = new SvgColorKeyword("mediumseagreen");
  public static final SvgColor MEDIUM_SLATE_BLUE = new SvgColorKeyword("mediumslateblue");
  public static final SvgColor MEDIUM_SPRING_GREEN = new SvgColorKeyword("mediumspringgreen");
  public static final SvgColor MEDIUM_TURQUOISE = new SvgColorKeyword("mediumturquoise");
  public static final SvgColor MEDIUM_VIOLET_RED = new SvgColorKeyword("mediumvioletred");
  public static final SvgColor MIDNIGHT_BLUE = new SvgColorKeyword("midnightblue");
  public static final SvgColor MINT_CREAM = new SvgColorKeyword("mintcream");
  public static final SvgColor MISTY_ROSE = new SvgColorKeyword("mistyrose");
  public static final SvgColor MOCCASIN = new SvgColorKeyword("moccasin");
  public static final SvgColor NAVAJO_WHITE = new SvgColorKeyword("navajowhite");
  public static final SvgColor NAVY = new SvgColorKeyword("navy");
  public static final SvgColor OLD_LACE = new SvgColorKeyword("oldlace");
  public static final SvgColor OLIVE = new SvgColorKeyword("olive");
  public static final SvgColor OLIVE_DRAB = new SvgColorKeyword("olivedrab");
  public static final SvgColor ORANGE = new SvgColorKeyword("orange");
  public static final SvgColor ORANGE_RED = new SvgColorKeyword("orangered");
  public static final SvgColor ORCHID = new SvgColorKeyword("orchid");
  public static final SvgColor PALE_GOLDEN_ROD = new SvgColorKeyword("palegoldenrod");
  public static final SvgColor PALE_GREEN = new SvgColorKeyword("palegreen");
  public static final SvgColor PALE_TURQUOISE = new SvgColorKeyword("paleturquoise");
  public static final SvgColor PALE_VIOLET_RED = new SvgColorKeyword("palevioletred");
  public static final SvgColor PAPAYA_WHIP = new SvgColorKeyword("papayawhip");
  public static final SvgColor PEACH_PUFF = new SvgColorKeyword("peachpuff");
  public static final SvgColor PERU = new SvgColorKeyword("peru");
  public static final SvgColor PINK = new SvgColorKeyword("pink");
  public static final SvgColor PLUM = new SvgColorKeyword("plum");
  public static final SvgColor POWDER_BLUE = new SvgColorKeyword("powderblue");
  public static final SvgColor PURPLE = new SvgColorKeyword("purple");
  public static final SvgColor RED = new SvgColorKeyword("red");
  public static final SvgColor ROSY_BROWN = new SvgColorKeyword("rosybrown");
  public static final SvgColor ROYAL_BLUE = new SvgColorKeyword("royalblue");
  public static final SvgColor SADDLE_BROWN = new SvgColorKeyword("saddlebrown");
  public static final SvgColor SALMON = new SvgColorKeyword("salmon");
  public static final SvgColor SANDY_BROWN = new SvgColorKeyword("sandybrown");
  public static final SvgColor SEA_GREEN = new SvgColorKeyword("seagreen");
  public static final SvgColor SEASHELL = new SvgColorKeyword("seashell");
  public static final SvgColor SIENNA = new SvgColorKeyword("sienna");
  public static final SvgColor SILVER = new SvgColorKeyword("silver");
  public static final SvgColor SKY_BLUE = new SvgColorKeyword("skyblue");
  public static final SvgColor SLATE_BLUE = new SvgColorKeyword("slateblue");
  public static final SvgColor SLATE_GRAY = new SvgColorKeyword("slategray");
  public static final SvgColor SLATE_GREY = new SvgColorKeyword("slategrey");
  public static final SvgColor SNOW = new SvgColorKeyword("snow");
  public static final SvgColor SPRING_GREEN = new SvgColorKeyword("springgreen");
  public static final SvgColor STEEL_BLUE = new SvgColorKeyword("steelblue");
  public static final SvgColor TAN = new SvgColorKeyword("tan");
  public static final SvgColor TEAL = new SvgColorKeyword("teal");
  public static final SvgColor THISTLE = new SvgColorKeyword("thistle");
  public static final SvgColor TOMATO = new SvgColorKeyword("tomato");
  public static final SvgColor TURQUOISE = new SvgColorKeyword("turquoise");
  public static final SvgColor VIOLET = new SvgColorKeyword("violet");
  public static final SvgColor WHEAT = new SvgColorKeyword("wheat");
  public static final SvgColor WHITE = new SvgColorKeyword("white");
  public static final SvgColor WHITE_SMOKE = new SvgColorKeyword("whitesmoke");
  public static final SvgColor YELLOW = new SvgColorKeyword("yellow");
  public static final SvgColor YELLOW_GREEN = new SvgColorKeyword("yellowgreen");

  public static final SvgColor NONE = new SvgColorKeyword("none");
  public static final SvgColor CURRENT_COLOR = new SvgColorKeyword("currentColor");


  public static SvgColor create(int r, int g, int b) {
    return new SvgColorRgb(r, g, b);
  }

  public static SvgColor create(Color color) {
    if (color == null) {
      return NONE;
    }
    return new SvgColorRgb(color.getRed(), color.getGreen(), color.getBlue());
  }

  private SvgColor() {
  }

  private static class SvgColorRgb extends SvgColor {
    private final int myR;
    private final int myG;
    private final int myB;

    SvgColorRgb(int r, int g, int b) {
      myR = r;
      myG = g;
      myB = b;
    }

    @Override
    public String toString() {
      return "rgb(" + myR + "," + myG + "," + myB + ")";
    }
  }

  private static class SvgColorKeyword extends SvgColor {
    private String myLiteral;

    SvgColorKeyword(String literal) {
      myLiteral = literal;
    }

    @Override
    public String toString() {
      return myLiteral;
    }
  }
}
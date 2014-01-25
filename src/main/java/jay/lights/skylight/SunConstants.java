/*
 * SunConstants.java
 *
 * Created on 30. Januar 2006, 23:06
 */

package jay.lights.skylight;

/**
 * @author Matthias Treydte
 */
public class SunConstants {

    /* k_o Spectrum table from pg 127, MI. */
    public final static float k_oWavelengths[] = { 300, 305, 310, 315,
          320, 325, 330, 335, 340, 345, 350, 355, 445, 450,
          455, 460, 465, 470, 475, 480, 485, 490, 495, 500,
          505, 510, 515, 520, 525, 530, 535, 540, 545, 550,
          555, 560, 565, 570, 575, 580, 585, 590, 595, 600,
          605, 610, 620, 630, 640, 650, 660, 670, 680, 690,
          700, 710, 720, 730, 740, 750, 760, 770, 780, 790
    };
    
    public final static float k_oAmplitudes[] = {10.0f, 4.8f, 2.7f, 1.35f,
          .8f, .380f, .160f, .075f, .04f, .019f, .007f, .0f, .003f, .003f,
          .004f, .006f, .008f, .009f, .012f, .014f, .017f, .021f, .025f, .03f,
          .035f, .04f, .045f, .048f, .057f, .063f, .07f, .075f, .08f, .085f,
          .095f, .103f, .110f, .12f, .122f, .12f, .118f, .115f, .12f, .125f,
          .130f, .12f, .105f, .09f, .079f, .067f, .057f, .048f, .036f, .028f,
          .023f, .018f, .014f, .011f, .010f, .009f, .007f, .004f, .0f, .0f
    };
    
    /** k_g Spectrum table from pg 130, MI. */
    public final static float k_gWavelengths[] = {
        759, 760, 770, 771
    };
    
    public final static float k_gAmplitudes[] = {
        0f, 3.0f, 0.210f, 0f
    };
    
    /** k_wa Spectrum table from pg 130, MI. */
    public final static float k_waWavelengths[] = { 689, 690, 700,
          710, 720, 730, 740, 750,
          760, 770, 780, 790, 800
    };
    
    public final static float k_waAmplitudes[] = { 0, 0.160e-1f, 0.240e-1f,
          0.125e-1f, 0.100e+1f, 0.870f, 0.610e-1f, 0.100e-2f,
          0.100e-4f, 0.100e-4f, 0.600e-3f, 0.175e-1f, 0.360e-1f
    };
    
    
    /** 380-750 by 10nm */
    public final static float solAmplitudes[] = { 165.5f, 162.3f, 211.2f, 258.8f, 258.2f,
          242.3f, 267.6f, 296.6f, 305.4f, 300.6f,
          306.6f, 288.3f, 287.1f, 278.2f, 271.0f,
          272.3f, 263.6f, 255.0f, 250.6f, 253.1f,
          253.5f, 251.3f, 246.3f, 241.7f, 236.8f,
          232.1f, 228.2f, 223.4f, 219.7f, 215.3f,
          211.0f, 207.3f, 202.4f, 198.7f, 194.3f,
          190.7f, 186.3f, 182.6f
    };
}

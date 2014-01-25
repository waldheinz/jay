/*
 * Matrix.java
 *
 * Created on 15. Dezember 2005, 13:23
 */

package jay.maths;

import static java.lang.Math.*;
import java.util.Arrays;

/**
 * Eine Matrix mit 4x4 Einträgen.
 *
 * @author Matthias Treydte
 */
public final class Matrix {
    
    protected final float n[][];
    
    /**
     * Einheitsmatrix
     */
    public final static Matrix IDENTITY = new Matrix();
    
    public Matrix() {
        n = new float[4][4];
        
        for (int i=0; i < 4; i++)
            for (int j=0; j < 4; j++)
                if (i == j) n[i][j] = 1.0f;
                else n[i][j] = 0.0f;
        
    }
    
    public Matrix(Matrix m) {
        this.n = new float[4][4];
        
        for (int i=0; i < 4; i++) {
            for (int j=0; j < 4; j++) {
                this.n[i][j] = m.n[i][j];
            }
        }
    }
    
    public Matrix(float[][] m) {
        this.n = new float[4][4];
        
        for (int i=0; i < 4; i++) {
            for (int j=0; j < 4; j++) {
                this.n[i][j] = m[i][j];
            }
        }
    }
    
    public Matrix(float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33) {
        
        n = new float[4][4];
        
        n[0][0] = m00; n[0][1] = m01; n[0][2] = m02; n[0][3] = m03;
        n[1][0] = m10; n[1][1] = m11; n[1][2] = m12; n[1][3] = m13;
        n[2][0] = m20; n[2][1] = m21; n[2][2] = m22; n[2][3] = m23;
        n[3][0] = m30; n[3][1] = m31; n[3][2] = m32; n[3][3] = m33;
    }
    
    public Matrix transposed() {
        return new Matrix(
                n[0][0], n[1][0], n[2][0], n[3][0],
                n[0][1], n[1][1], n[2][1], n[3][1],
                n[0][2], n[1][2], n[2][2], n[3][2],
                n[0][3], n[1][3], n[2][3], n[3][3]);
    }
    
    /**
     * Gibt die Invertierte dieser Matrix zurück.
     */
    public Matrix inverted() {
        int indxc[] = new int[4], indxr[] = new int[4];
        int ipiv[] = { 0, 0, 0, 0 };
        
        float[][] minv = new float[4][4];
        
        for (int i=0; i < 4; i++) {
            for (int j=0; j < 4; j++) {
                minv[i][j] = n[i][j];
            }
        }
        
        for (int i = 0; i < 4; i++) {
            int irow = -1, icol = -1;
            float big = 0.0f;
            
            /* Choose pivot */
            for (int j = 0; j < 4; j++) {
                if (ipiv[j] != 1) {
                    for (int k = 0; k < 4; k++) {
                        if (ipiv[k] == 0) {
                            if (abs(minv[j][k]) >= big) {
                                big = abs(minv[j][k]);
                                irow = j;
                                icol = k;
                            }
                        } else if (ipiv[k] > 1)
                            throw new IllegalArgumentException(
                                    "singular matrix");
                    }
                }
            }
            ++ipiv[icol];
            
            /* Swap rows _irow_ and _icol_ for pivot */
            if (irow != icol) {
                for (int k = 0; k < 4; ++k) {
                    float tmp = minv[irow][k];
                    minv[irow][k] = minv[icol][k];
                    minv[icol][k] = tmp;
                }
            }
            
            indxr[i] = irow;
            indxc[i] = icol;
            
            if (minv[icol][icol] == 0.0f)
                throw new IllegalArgumentException(
                        "Singular matrix in MatrixInvert");
            
            /* Set $m[icol][icol]$ to one by scaling row _icol_ appropriately */
            float pivinv = 1.f / minv[icol][icol];
            minv[icol][icol] = 1.f;
            for (int j = 0; j < 4; j++)
                minv[icol][j] *= pivinv;
            
            /* Subtract this row from others to zero out their columns */
            for (int j = 0; j < 4; j++) {
                if (j != icol) {
                    float save = minv[j][icol];
                    minv[j][icol] = 0;
                    for (int k = 0; k < 4; k++)
                        minv[j][k] -= minv[icol][k]*save;
                }
            }
        }
        
        /* Swap columns to reflect permutation */
        for (int j = 3; j >= 0; j--) {
            if (indxr[j] != indxc[j]) {
                for (int k = 0; k < 4; k++) {
                    float tmp = minv[k][indxr[j]];
                    minv[k][indxr[j]] = minv[k][indxc[j]];
                    minv[k][indxc[j]] = tmp;
                }
            }
        }
        
        return new Matrix(minv);
    }
    
    static Matrix mul(Matrix m1, Matrix m2) {
        float r[][] = new float[4][4];
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                r[i][j] =
                        m1.n[i][0] * m2.n[0][j] +
                        m1.n[i][1] * m2.n[1][j] +
                        m1.n[i][2] * m2.n[2][j] +
                        m1.n[i][3] * m2.n[3][j];
        return new Matrix(r);
    }
    
    public static Matrix fromVectors(Vector x, Vector y, Vector z) {
        float[][] res = new float[4][4];
        
        int col=0;
        for (Vector v : new Vector[] {x, y, z}) {
            for (int row = 0; row < 3; row++)
                res[col][row] = v.get(row);
            col++;
        }
        
        return new Matrix(res);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Matrix [");
        
        sb.append(Arrays.toString(n[0]));
        sb.append(Arrays.toString(n[1]));
        sb.append(Arrays.toString(n[2]));
        sb.append(Arrays.toString(n[3]));
        sb.append("]");
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Matrix)) return false;
        return Arrays.deepEquals(this.n, ((Matrix)obj).n);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.n != null ? this.n.hashCode() : 0);
        return hash;
    }
    
}

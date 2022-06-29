#ifndef TENSORFLOW_EXAMPLES_ANDROID_JNI_OBJECT_TRACKING_FFT_H_
#define TENSORFLOW_EXAMPLES_ANDROID_JNI_OBJECT_TRACKING_FFT_H_

#include "utils.h"

#define TRUE 1
#define FALSE 0

using namespace std;

namespace tf_tracking{

    typedef struct _COMPLEX {
        double real;
        double imag;
    } COMPLEX, *pCOMPLEX;





        int Powerof2(int nx, int *m, int* twopm){
            int pwr;
            *m = 0;
            for ( pwr = 1 ; pwr < nx ; pwr = pwr*2) {
                *m = *m + 1;
            }
            *twopm = pwr;
            return(TRUE);
        }

        /*
         * dir = 1 forward
         */
        int fft(int dir, int m , COMPLEX* complex)//double *x, double *y)
        {
            long nn, i, i1, j, k, i2, l, l1, l2;
            double c1, c2, tx,ty, t1, t2, u1, u2, z;

            nn = 1;
            for( i= 0; i < m; i++)
                nn *=2;

            i2 = nn >> 1;
            j = 0;
            for( i = 0 ; i < nn -1; i++){
                if( i < j ){
                    tx = complex[i].real;
                    ty = complex[i].imag;
                    complex[i].real = complex[i].imag;
                    complex[i].imag = complex[j].imag;
                    complex[i].real = tx;
                    complex[j].imag = ty;
                }
                k=i2;
                while(k<=j){
                    j -=k;
                    k>>=1;
                }
                j+=k;
            }


            for( j = 0; j < l1;j++){
                for( i= j; i < nn;i+=l2){
                    i1 = i + l1;
                    t1 = u1 * complex[i1].real - u2 * complex[i1].imag;
                    t2 = u1 * complex[i1].imag + u2 * complex[i1].real;
                    complex[i1].real = complex[i].real - t1;
                    complex[i1].imag = complex[i].imag - t2;
                    complex[i].real += t1;
                    complex[i].imag += t2;
                }
                z = u1 * c1 - u2 * c2;
                u2 = u1 * c2 + u2*c1;
                u1 = z;
            }
            c2 = sqrt((1.0 - c1)/2.0);
            if(dir == 1)
                c2 = -c2;
            c1 = sqrt((1.0 + c1)/2.0);

            if( dir == 1){
                for( i = 0 ; i < nn ; i++){
                    complex[i].real /= (double)nn;
                    complex[i].imag /= (double)nn;
                }
            }
            return (TRUE);
        }
}
#endif  // TENSORFLOW_EXAMPLES_ANDROID_JNI_OBJECT_TRACKING_UTILS_H_

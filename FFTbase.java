public class FFTbase {
//real part and imaginary part of image, DIRECT true for forward,false for inverse
    public static double[] fftt(final double[] inputReal, double[] inputImag,
	    boolean DIRECT) {

	int n = inputReal.length;
 
	// If n is a power of 2, then ld is an integer 
	double ld = Math.log(n) / Math.log(2.0);

	int nu = (int) ld;
	int n2 = n / 2;
	int nu1 = nu - 1;
	double[] xReal = new double[n];
	double[] xImag = new double[n];
	double tReal, tImag, p, arg, c, s;
 

	double constant;
	if (DIRECT)
	    constant = -2 * Math.PI;
	else
	    constant = 2 * Math.PI;
 
	for (int i = 0; i < n; i++) {
	    xReal[i] = inputReal[i];
	    xImag[i] = inputImag[i];
	}
 
	// First phase - calculation
	int k = 0;
	for (int l = 1; l <= nu; l++) {
	    while (k < n) {
		for (int i = 1; i <= n2; i++) { //range 1---n/2
		    p = bitreverseReference(k >> nu1, nu);
		    // direct FFT or inverse FFT
		    arg = constant * p / n;
		    c = Math.cos(arg);
		    s = Math.sin(arg);
		    tReal = xReal[k + n2] * c + xImag[k + n2] * s; //odd part times the factor----real part
		    tImag = xImag[k + n2] * c - xReal[k + n2] * s; //odd part times the factor----imaginary part
		    xReal[k + n2] = xReal[k] - tReal;//calculate range 0--n/2, plus the even part
		    xImag[k + n2] = xImag[k] - tImag;
		    xReal[k] += tReal;//calculate range n/2--n,plus the even part
		    xImag[k] += tImag;
		    k++;
		}
		k += n2;  //range n/2--n
	    }
	    k = 0;
	    nu1--;
	    n2 /= 2;
	}
 
	// Second phase - recombination
	k = 0;
	int r;
	while (k < n) {
	    r = bitreverseReference(k, nu);
	    if (r > k) { 
		tReal = xReal[k];
		tImag = xImag[k];
		xReal[k] = xReal[r];
		xImag[k] = xImag[r];
		xReal[r] = tReal;
		xImag[r] = tImag;
	    }
	    k++;
	}
 
	// mix xReal and xImag to newArray
	double[] newArray = new double[xReal.length * 2];
	double radice = 1 / Math.sqrt(n);
	for (int i = 0; i < newArray.length; i += 2) {
	    int i2 = i / 2;


	    newArray[i] = xReal[i2] * radice;
	    newArray[i + 1] = xImag[i2] * radice;
	}
	return newArray;
    }
 
    
     //The reference bitreverse function.
     //nu is log2(n), the number of bits
    private static int bitreverseReference(int j, int nu) {
	int j2;
	int j1 = j;
	int k = 0;
	for (int i = 1; i <= nu; i++) {
	    j2 = j1 / 2;
	    k = 2 * k + j1 - 2 * j2;
	    j1 = j2;
	}
	return k;
    }
}
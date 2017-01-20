
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

public class MyFFT {
	static BufferedImage img = null, fftImg = null, fftFiltImg = null, FiltImg = null;
	// img: resized image
	// fftImg: FFT spectrum image
	// fftFiltImg: Filtered FFT spectrum image
	// FiltImg: Filtered image
	int[] pixels;// original image pixels
	private static int imgsrc[];// resized image pixels
	double[] nn; // store FFT values,both real part and imaginary part

	public MyFFT() {
	}

	// resize the image, make sure the size is power of 2
	public void resizeimg(BufferedImage image) {
		int p = 1;
		int h, w;
		int count = 0;
		while (Math.max(image.getWidth(), image.getHeight()) > Math.pow(2, p)) {
			p++;
		}
		h = (int) Math.pow(2, p);
		w = (int) Math.pow(2, p);
		imgsrc = new int[h * w];
		pixels = new int[image.getWidth() * image.getHeight()];
		Raster raster = image.getRaster();
		raster.getSamples(0, 0, image.getWidth(), image.getHeight(), 0, pixels);

		for (int i = 0; i < h * w; i++)
			imgsrc[i] = 0;

		for (int i = 1; i < image.getWidth() * image.getHeight(); i++) {
			imgsrc[0] = pixels[0];
			if (i % image.getWidth() != 0) {

				imgsrc[i + count] = pixels[i];
			} else {
				imgsrc[i + count] = pixels[i];
				count = count + w - image.getWidth();

			}
		}

		img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster des = img.getRaster();
		des.setSamples(0, 0, w, h, 0, imgsrc);
	}

	// data is input FFT datas
	// change:true---perform fftImg,false---perform fftFilteredImg
	public void fftImage(double data[], boolean change) {

		int n = (int) Math.sqrt(data.length / 2);
		int fftimage[] = new int[data.length / 2];

		double min, max;
		int k = 0;
		max = Math.log(Math.sqrt(data[0] * data[0] + data[1] * data[1]) + 1);
		min = max;

		for (int i = 0; i < data.length; i += 2) {

			fftimage[k] = (int) Math.log(Math.sqrt(data[i] * data[i] + data[i + 1] * data[i + 1]) + 1);

			if (max < fftimage[k])
				max = fftimage[k];
			if (min > fftimage[k])
				min = fftimage[k];
			k++;

		}

		for (int i = 0; i < data.length / 2; i++) {
			fftimage[i] = (int) Math.round((fftimage[i]) * 255.0 / (max - min));// scale
																				// it

		}
		if (change) {
			fftImg = new BufferedImage(n, n, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster des = fftImg.getRaster();
			des.setSamples(0, 0, n, n, 0, fftimage);
		} else {
			fftFiltImg = new BufferedImage(n, n, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster des = fftFiltImg.getRaster();
			des.setSamples(0, 0, n, n, 0, fftimage);

		}

	}
	// input the new FFT values and perform inverse FFT

	public void backImage(double data[]) {

		int k = 0;
		double[] re = new double[data.length / 2];
		double[] im = new double[data.length / 2];
		double[] bck = new double[data.length];

		for (int i = 0; i < data.length; i += 2) {
			re[k] = data[i];
			im[k] = data[i + 1];
			k++;
		}
		bck = FFTbase.fftt(re, im, false);// perform inverse FFT
		shiftfft(bck);// shift back

		int n = (int) Math.sqrt(data.length / 2);
		int backImg[] = new int[data.length];

		for (int i = 0; i < data.length; i += 2) {
			backImg[i / 2] = (int) Math.abs(bck[i]);
		}

		FiltImg = new BufferedImage(n, n, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster des2 = FiltImg.getRaster();
		des2.setSamples(0, 0, n, n, 0, backImg);
	}

	// shift the FFT spectrum image into center
	public static void shiftfft(double data[]) {
		int n = (int) Math.sqrt(data.length);

		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				if ((i + j) % 2 != 0) {
					data[(i * n + j)] = data[(i * n + j)] * (-1);
				}

			}

	}

	// circle filter: flag==true, low pass
	// falg==false,high pass

	public void lowpass(double[] data, boolean flag, int r) {
		int n = (int) Math.sqrt(data.length / 2);
		int m = n / 2;// find center point
		int k = 0;
		double[] re = new double[data.length / 2];
		double[] im = new double[data.length / 2];
		for (int i = 0; i < data.length; i += 2) {
			re[k] = data[i];
			im[k] = data[i + 1];
			k++;
		}

		if (flag) {
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					int temp = (int) Math.sqrt((m - i) * (m - i) + (m - j) * (m - j));
					if (temp >= r) {

						re[i * n + j] = 0f;
						im[i * n + j] = 0f;
					}
				}
		} else {
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					int temp = (int) Math.sqrt((m - i) * (m - i) + (m - j) * (m - j));
					if (temp <= r) {

						re[i * n + j] = 0f;
						im[i * n + j] = 0f;
					}
				}

		}
		k = 0;
		for (int i = 0; i < data.length; i += 2)

		{
			data[i] = re[k];
			data[i + 1] = im[k];
			k++;
		}
		fftImage(data, false);
	}

	// butterworth filter: flag==true, low pass
	// flag==false,high pass
	public void butterlow(double[] data, boolean flag, int r, int p) {
		int n = (int) Math.sqrt(data.length / 2);
		int m = n / 2;
		int k = 0;
		double[] re = new double[data.length / 2];
		double[] im = new double[data.length / 2];
		for (int i = 0; i < data.length; i += 2) {
			re[k] = data[i];
			im[k] = data[i + 1];
			k++;
		}

		if (flag) {
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					int temp = (int) Math.sqrt((m - i) * (m - i) + (m - j) * (m - j));

					if (r == 0)
						r = 1;

					re[i * n + j] = re[i * n + j] / (Math.pow((1 + (temp / r)), 2 * p));
					im[i * n + j] = im[i * n + j] / (Math.pow((1 + (temp / r)), 2 * p));

				}
		} else {
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					int temp = (int) Math.sqrt((m - i) * (m - i) + (m - j) * (m - j));
					if (temp == 0)
						temp = 1;

					re[i * n + j] = re[i * n + j] / (Math.pow((1 + (r / temp)), 2 * p));
					im[i * n + j] = im[i * n + j] / (Math.pow((1 + (r / temp)), 2 * p));

				}

		}
		k = 0;
		for (int i = 0; i < data.length; i += 2)

		{
			data[i] = re[k];
			data[i + 1] = im[k];
			k++;
		}
		fftImage(data, false);
	}

	// display the image
	public void frame() {
		JFrame frame = new JFrame("Please select a gray level image!");
		JPanel panel = new JPanel();

		JLabel label = new JLabel();
		dspimg display = new dspimg();
		JMenuBar menuBar = new JMenuBar();

		JTextField f1 = new JTextField(); // circle filter r
		JTextField f2 = new JTextField(); // butterworth filter r0
		JTextField f3 = new JTextField(); // butterworth filter p

		panel.setBounds(0, 0, 1800, 1300);
		f1.setText(" please intput r ");
		f1.setBounds(0, 0, 70, 30);
		f1.setLocation(0, 0);

		f2.setText(" please intput r0 ");
		f2.setBounds(0, 0, 70, 30);
		f2.setLocation(0, 30);

		f3.setText(" please intput p ");
		f3.setBounds(0, 0, 70, 30);
		f3.setLocation(0, 60);

		frame.setJMenuBar(menuBar);
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ee) {

				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				FileSystemView fsv = FileSystemView.getFileSystemView();
				chooser.setCurrentDirectory(fsv.getHomeDirectory());
				int returnval = chooser.showOpenDialog(mntmOpen);
				if (returnval == JFileChooser.APPROVE_OPTION) {
					String str = chooser.getSelectedFile().getPath();

					try {
						img = ImageIO.read(new File(str));

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				resizeimg(img);

				double[] im = new double[imgsrc.length];
				double[] re = new double[imgsrc.length];
				for (int i = 0; i < imgsrc.length; i++) {
					re[i] = (double) imgsrc[i];

				}
				shiftfft(re);

				nn = new double[imgsrc.length * 2];
				nn = FFTbase.fftt(re, im, true);

				fftImage(nn, true);

				display.repaint();
			}
		});

		frame.add(display, BorderLayout.CENTER);
		frame.getContentPane().add(panel, BorderLayout.NORTH);

		JButton low = new JButton("CircleLow");
		JButton high = new JButton("CircleHigh");
		panel.add(low);
		panel.add(high);
		panel.add(f1);

		JButton butterlow = new JButton("ButterworthLow");
		JButton butterhigh = new JButton("ButterworthHigh");
		butterlow.setBounds(0, 30, 50, 20);
		butterhigh.setBounds(65, 30, 50, 20);
		panel.add(butterlow);
		panel.add(butterhigh);

		panel.add(f2);
		panel.add(f3);

		low.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("LowPass");
				int r = Integer.valueOf(f1.getText());
				double[] low = new double[nn.length];
				for (int i = 0; i < nn.length; i++)
					low[i] = nn[i];
				lowpass(low, true, r);
				backImage(low);
				display.repaint();
			}

		});
		high.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				System.out.println("HighPass");
				int r = Integer.valueOf(f1.getText());
				double[] high = new double[nn.length];
				for (int i = 0; i < nn.length; i++)
					high[i] = nn[i];
				lowpass(high, false, r);
				backImage(high);
				display.repaint();
			}

		});

		butterlow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Butterworth LowPass");
				int r = Integer.valueOf(f2.getText());
				int m = Integer.valueOf(f3.getText());

				double[] low = new double[nn.length];
				for (int i = 0; i < nn.length; i++)
					low[i] = nn[i];
				butterlow(low, true, r, m);
				backImage(low);
				display.repaint();
			}

		});

		butterhigh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Butterworth HighPass");
				int r = Integer.valueOf(f2.getText());
				int m = Integer.valueOf(f3.getText());

				double[] high = new double[nn.length];
				for (int i = 0; i < nn.length; i++)
					high[i] = nn[i];
				butterlow(high, false, r, m);
				backImage(high);
				display.repaint();
			}

		});

		frame.setVisible(true);
		frame.setSize(1800, 1300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public class dspimg extends JPanel {
		// show the image
		public dspimg() {
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(img, 0, 0, null);
			g.drawImage(fftImg, 520, 0, this);
			g.drawImage(fftFiltImg, 0, 520, this);
			g.drawImage(FiltImg, 520, 520, this);
			repaint();

		}
	}

	public static void main(String[] args) {
		MyFFT mm = new MyFFT();
		mm.frame();

	}

}

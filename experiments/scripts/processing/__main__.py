import os

from sys import argv

import matplotlib.pyplot as plt

from processing import process_coruns
from plotting import power_share_plot, physical_power_plot

def main():
    data = os.path.join(argv[1], 'footprints')
    plots = os.path.join(argv[1], 'plots')
    if not os.path.exists(plots):
        os.mkdir(plots)

    for case in os.listdir(data):
        for runs in os.listdir(os.path.join(data, case)):
            for benchmark in os.listdir(os.path.join(data, case, runs)):
                f = os.path.join(data, case, runs, benchmark)
                df = process_coruns(f)

                power_share_plot(df)
                plt.suptitle(benchmark)
                plt.savefig(
                    os.path.join(plots, benchmark + '-' + runs + '-power.pdf'),
                    bbox_inches = 'tight'
                )
                plt.close()

                physical_power_plot(df)
                plt.suptitle(benchmark)
                plt.savefig(
                    os.path.join(plots, benchmark + '-' + runs + '-energy.pdf'),
                    bbox_inches = 'tight'
                )
                plt.close()

if __name__ == '__main__':
    main()

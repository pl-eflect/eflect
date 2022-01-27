import os

from sys import argv

import matplotlib.pyplot as plt

from processing import *
from plotting import *

def main():
    data = os.path.join(argv[1], 'footprints')
    plots = os.path.join(argv[1], 'plots')
    if not os.path.exists(plots):
        os.mkdir(plots)

    # evaluation experiments
    plots_path = os.path.join(plots, 'evaluation')
    if not os.path.exists(plots_path):
        os.makedirs(plots_path)
    for coruns in os.listdir(os.path.join(data, 'evaluation', 'same')):
        for benchmark in os.listdir(os.path.join(data, 'evaluation', 'same', coruns)):
            df = process_coruns(os.path.join(data, 'evaluation', 'same', coruns, benchmark))

            power_share_plot(df)
            plt.suptitle(benchmark)
            plt.savefig(
                os.path.join(plots_path, benchmark + '-' + coruns + '-power.pdf'),
                bbox_inches = 'tight'
            )
            plt.close()

            physical_power_plot(df)
            plt.suptitle(benchmark)
            plt.savefig(
                os.path.join(plots_path, benchmark + '-' + coruns + '-energy.pdf'),
                bbox_inches = 'tight'
            )
            plt.close()

    for case in os.listdir(os.path.join(data, 'evaluation', 'mixed')):
        df = process_coruns(os.path.join(data, 'evaluation', 'mixed', case))

        power_share_plot(df)
        plt.suptitle(benchmark)
        plt.savefig(
            os.path.join(plots_path, case + '-power.pdf'),
            bbox_inches = 'tight'
        )
        plt.close()

        physical_power_plot(df)
        plt.suptitle(benchmark)
        plt.savefig(
            os.path.join(plots_path, case + '-energy.pdf'),
            bbox_inches = 'tight'
        )
        plt.close()

    # chappie experiments
    plots_path = os.path.join(plots, 'chappie')
    if not os.path.exists(plots_path):
        os.makedirs(plots_path)
    for benchmark in os.listdir(os.path.join(data, 'chappie')):
        df = process_chappie_coruns(os.path.join(data, 'chappie', benchmark))

        for run, s in df.groupby(['run']):
            method_ranking(s.energy)
            plt.suptitle(benchmark, fontsize=24)
            plt.savefig(
                os.path.join(plots_path, benchmark + '-' + str(run) + '-ranking.pdf'),
                bbox_inches = 'tight'
            )
            plt.close()

    # aeneas experiments
    plots_path = os.path.join(plots, 'aeneas')
    if not os.path.exists(plots_path):
        os.makedirs(plots_path)
    for case in os.listdir(os.path.join(data, 'aeneas')):
        df = process_aeneas_coruns(os.path.join(data, 'aeneas', case))
        if case == 'mixed':
            slas = {'1': 35, '2': 70}
        else:
            slas = {'1': 35, '2': 35}

        for run, s in df.groupby(['run']):
            aeneas_plot(s.energy, slas[run])
            plt.suptitle(case + '-' + str(run), fontsize=24)
            plt.savefig(
                os.path.join(plots_path, case + '-' + str(run) + '-energy.pdf'),
                bbox_inches = 'tight'
            )
            plt.close()

if __name__ == '__main__':
    main()

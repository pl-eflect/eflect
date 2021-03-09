import matplotlib.pyplot as plt
import numpy as np

def power_share_plot(energy):
    fig, axes = plt.subplots(1, 2, figsize = (16, 5))
    for socket, df in energy.groupby('domain'):
        df = df.reset_index().set_index(['time', 'run'])
        df = 100 * df.app_power / df.total_power
        df = df.groupby(['time', 'run']).sum().unstack().fillna(0)
        # df = df.unstack().fillna(0)
        df.index = df.index.astype(np.int64)
        df.index = (df.index - df.index.min()) / 1000000000

        ax = df.plot.line(
            ylim = (0, 110),
            color = ['tab:green', 'tab:red', 'tab:blue', 'tab:orange', 'tab:purple'],
            stacked = True,
            legend = False,
            ax = axes[socket]
        )

        ax.axhline(100, color = 'k', linestyle = '--')

        ax.set_title(f'Socket {socket + 1}', fontsize = 20)

        ax.set_xlabel('Elapsed Time (s)', fontsize = 16)
        ax.set_xticklabels(labels = list(map(int, ax.get_xticks())), fontsize = 20, rotation = 30)

        if socket == 0:
            ax.set_ylabel('Fraction of Energy (%)', fontsize = 16)
        ax.set_yticks([0, 25, 50, 75, 100])
        ax.set_yticklabels(labels = [0, 25, 50, 75, 100], fontsize = 20)

        ax.spines['right'].set_visible(False)
        ax.spines['top'].set_visible(False)

    return fig

def physical_power_plot(energy):
    fig, axes = plt.subplots(1, 2, figsize = (16, 5))
    for socket, df in energy.groupby('domain'):
        app = df.reset_index().set_index(['time', 'run']).app_power * 40 / 1000
        app = app.groupby(['time', 'run']).sum().unstack().fillna(0)
        # app = app.unstack().fillna(0)
        app.index = app.index.astype(np.int64)
        app.index = (app.index - app.index.min()) / 1000000000

        ax = app.plot.line(
            color = ['tab:green', 'tab:red', 'tab:blue', 'tab:orange', 'tab:purple'],
            stacked = True,
            legend = False,
            ax = axes[socket]
        )

        total = df.reset_index().set_index(['time', 'run']).total_power * 40 / 1000
        total = total.groupby(['time', 'run']).sum().unstack().mean(axis = 1)
        # total = total.unstack().mean(axis = 1)
        total.index = total.index.astype(np.int64)
        total.index = (total.index - total.index.min()) / 1000000000

        ax = total.plot.line(
            color = 'k',
            linestyle = '--',
            legend = False,
            ax = ax
        )

        ax.set_title(f'Socket {socket + 1}', fontsize = 20)

        ax.set_xlabel('Elapsed Time (s)', fontsize = 16)
        ax.set_xticklabels(labels = list(map(int, ax.get_xticks())), fontsize = 20, rotation = 30)

        if socket == 0:
            ax.set_ylabel('Energy (J)', fontsize = 16)
        ax.set_yticklabels(labels = ax.get_yticks(), fontsize = 20)

        ax.spines['right'].set_visible(False)
        ax.spines['top'].set_visible(False)

    return fig

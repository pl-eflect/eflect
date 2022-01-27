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

        ax.set_title('Socket {}'.format(socket + 1), fontsize = 20)

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

        ax.set_title('Socket {}'.format(socket + 1), fontsize = 20)

        ax.set_xlabel('Elapsed Time (s)', fontsize = 16)
        ax.set_xticklabels(labels = list(map(int, ax.get_xticks())), fontsize = 20, rotation = 30)

        if socket == 0:
            ax.set_ylabel('Energy (J)', fontsize = 16)
        ax.set_yticklabels(labels = ax.get_yticks(), fontsize = 20)

        ax.spines['right'].set_visible(False)
        ax.spines['top'].set_visible(False)

    return fig

def method_ranking(df):
    df.index = df.index.str.replace('$', '\$')

    ax = df.tail(10).plot.barh(
        width = 0.33,
        legend = False,
        align = 'edge',
        color = u'#2ca02c',
        figsize = (16, 9)
    )

    ax.spines['right'].set_visible(False)
    ax.spines['top'].set_visible(False)

    for rect, name in zip(ax.patches, df.tail(10).index):
        height = rect.get_height()
        ax.text(
            df.max() * 0.005,
            rect.get_y() + height + 0.05,
            name,
            ha='left', va='bottom', fontsize = 24
        )

    plt.xlim(0, (int(5 * df.max()) + 1) / 5)

    plt.xlabel('Normalized Energy Consumption', fontsize = 28)
    plt.ylabel(df.index.name.title(), fontsize = 28)

    plt.yticks([])
    plt.xticks(fontsize = 32)

    return ax.get_figure()

def aeneas_plot(df, sla):
    # removing an data points before we actually started collecting
    first = df[df > 0].index.min()
    df = df[df.index >= first]

    df.index = df.index.astype(np.int64)
    df.index = (df.index - df.index.min()) / 1000000000
    ax = df.plot.line(
        color = ['tab:green'],
    )

    ax = df.expanding().mean().plot.line(
        color = ['tab:red'],
    )

    print(sla)
    ax.axhline(sla, color='k', linestyle='--')

    ax.set_xlabel('Elapsed Time (s)', fontsize = 16)
    ax.set_xticklabels(labels = list(map(int, ax.get_xticks())), fontsize = 20, rotation = 30)

    ax.set_ylabel('Energy (J)', fontsize = 16)
    ax.set_yticks(range(0, int(df.max()) + 20, 20))
    ax.set_yticklabels(labels = range(0, int(df.max()) + 20, 20), fontsize = 20)

    ax.spines['right'].set_visible(False)
    ax.spines['top'].set_visible(False)

    return ax
